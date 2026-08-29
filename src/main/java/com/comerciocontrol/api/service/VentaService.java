package com.comerciocontrol.api.service;

import com.comerciocontrol.api.dto.ItemVentaRequest;
import com.comerciocontrol.api.dto.VentaRequest;
import com.comerciocontrol.api.exception.RecursoNoEncontradoException;
import com.comerciocontrol.api.exception.ReglaNegocioException;
import com.comerciocontrol.api.model.Caja;
import com.comerciocontrol.api.model.Producto;
import com.comerciocontrol.api.model.Venta;
import com.comerciocontrol.api.repository.ClienteRepository;
import com.comerciocontrol.api.repository.MovimientoRepository;
import com.comerciocontrol.api.repository.ProductoRepository;
import com.comerciocontrol.api.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reglas de negocio del punto de venta (POS).
 *
 * <p>El registro de una venta es una <b>transaccion atomica</b>: si cualquier paso
 * falla (por ejemplo, stock insuficiente en el tercer producto del carrito) se
 * revierte todo y la base de datos queda como estaba.</p>
 *
 * <p>Reglas aplicadas, tomadas de los requerimientos del proyecto:</p>
 * <ol>
 *   <li>No se puede vender sin una caja abierta.</li>
 *   <li>El stock nunca puede quedar negativo.</li>
 *   <li>Los importes los calcula el servidor; nunca se confia en el cliente.</li>
 *   <li>Cada venta descuenta stock, deja movimiento de salida y traza de auditoria.</li>
 * </ol>
 */
@Service
public class VentaService {

    private final VentaRepository ventas;
    private final ProductoRepository productos;
    private final ClienteRepository clientes;
    private final MovimientoRepository movimientos;
    private final CajaService cajaService;
    private final AuditoriaService auditoria;
    private final double ivaPorcentaje;

    public VentaService(VentaRepository ventas, ProductoRepository productos,
                        ClienteRepository clientes, MovimientoRepository movimientos,
                        CajaService cajaService, AuditoriaService auditoria,
                        @Value("${app.negocio.iva-porcentaje}") double ivaPorcentaje) {
        this.ventas = ventas;
        this.productos = productos;
        this.clientes = clientes;
        this.movimientos = movimientos;
        this.cajaService = cajaService;
        this.auditoria = auditoria;
        this.ivaPorcentaje = ivaPorcentaje;
    }

    /** Ventas del rango indicado. Si no se envian fechas se usa el dia de hoy. */
    public List<Venta> listar(String desde, String hasta) {
        String hoy = LocalDate.now().toString();
        return ventas.listar(desde == null || desde.isBlank() ? hoy : desde,
                hasta == null || hasta.isBlank() ? hoy : hasta);
    }

    public Venta obtener(int id) {
        return ventas.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("venta", id));
    }

    /**
     * Registra una venta completa.
     *
     * @param peticion carrito enviado por el punto de venta
     * @param usuarioId vendedor autenticado que registra la operacion
     * @return la venta persistida con su detalle y sus totales calculados
     */
    @Transactional
    public Venta registrar(VentaRequest peticion, int usuarioId) {
        // 1. Regla: no se vende sin caja abierta.
        Caja caja = cajaService.exigirCajaAbierta();

        // 2. Regla: el cliente indicado debe existir (si se envio).
        if (peticion.clienteId() != null && !clientes.existe(peticion.clienteId())) {
            throw new RecursoNoEncontradoException("cliente", peticion.clienteId());
        }

        // 3. Se agrupan las lineas repetidas del mismo producto para validar el stock una sola vez.
        Map<Integer, Integer> cantidadPorProducto = new HashMap<>();
        for (ItemVentaRequest item : peticion.items()) {
            cantidadPorProducto.merge(item.productoId(), item.cantidad(), Integer::sum);
        }

        // 4. Se calculan los importes en el servidor con el precio vigente en base de datos.
        double subtotal = 0d;
        Map<Integer, Producto> catalogo = new HashMap<>();
        for (Map.Entry<Integer, Integer> linea : cantidadPorProducto.entrySet()) {
            Producto producto = productos.buscarPorId(linea.getKey())
                    .orElseThrow(() -> new RecursoNoEncontradoException("producto", linea.getKey()));
            if (Boolean.FALSE.equals(producto.activo())) {
                throw new ReglaNegocioException("El producto " + producto.codigo() + " - "
                        + producto.nombre() + " esta dado de baja y no se puede vender");
            }
            catalogo.put(producto.id(), producto);
            subtotal += producto.precioVenta() * linea.getValue();
        }

        double descuento = peticion.descuentoSeguro();
        if (descuento > subtotal) {
            throw new ReglaNegocioException("El descuento (" + descuento
                    + ") no puede superar el subtotal de la venta (" + redondear(subtotal) + ")");
        }

        double base = redondear(subtotal - descuento);
        double iva = redondear(base * ivaPorcentaje / 100d);
        double total = redondear(base + iva);

        // 5. Cabecera de la venta.
        int ventaId = ventas.crearCabecera(redondear(subtotal), descuento, iva, total,
                usuarioId, peticion.clienteId(), caja.id());

        // 6. Detalle, descuento de stock y kardex. El UPDATE condicionado garantiza
        //    que el stock nunca quede negativo aunque haya peticiones simultaneas.
        for (Map.Entry<Integer, Integer> linea : cantidadPorProducto.entrySet()) {
            Producto producto = catalogo.get(linea.getKey());
            int cantidad = linea.getValue();

            if (productos.descontarStock(producto.id(), cantidad) == 0) {
                throw new ReglaNegocioException("Stock insuficiente de " + producto.codigo() + " - "
                        + producto.nombre() + ". Disponible: " + producto.stockActual()
                        + ", solicitado: " + cantidad);
            }

            ventas.agregarDetalle(ventaId, producto.id(), cantidad, producto.precioVenta());
            movimientos.registrar(producto.id(), "salida", cantidad,
                    producto.stockActual() - cantidad, usuarioId, "Venta #" + ventaId);
        }

        // 7. Traza de auditoria.
        auditoria.registrar(usuarioId, "REGISTRAR_VENTA", "ventas",
                "Venta #" + ventaId + " por " + total + " con " + cantidadPorProducto.size()
                        + " producto(s), caja id " + caja.id());

        return obtener(ventaId);
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
