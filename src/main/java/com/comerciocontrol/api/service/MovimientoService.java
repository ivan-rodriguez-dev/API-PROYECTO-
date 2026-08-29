package com.comerciocontrol.api.service;

import com.comerciocontrol.api.dto.MovimientoRequest;
import com.comerciocontrol.api.exception.RecursoNoEncontradoException;
import com.comerciocontrol.api.exception.ReglaNegocioException;
import com.comerciocontrol.api.model.Movimiento;
import com.comerciocontrol.api.model.Producto;
import com.comerciocontrol.api.repository.MovimientoRepository;
import com.comerciocontrol.api.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Reglas de negocio de los movimientos de stock (kardex).
 *
 * <ul>
 *   <li><b>entrada:</b> suma unidades al inventario (compra a proveedor, devolucion).</li>
 *   <li><b>salida:</b> resta unidades; se rechaza si no hay stock suficiente.</li>
 *   <li><b>ajuste:</b> fija el stock al valor contado en un inventario fisico.</li>
 * </ul>
 */
@Service
public class MovimientoService {

    private final MovimientoRepository movimientos;
    private final ProductoRepository productos;
    private final AuditoriaService auditoria;

    public MovimientoService(MovimientoRepository movimientos, ProductoRepository productos,
                             AuditoriaService auditoria) {
        this.movimientos = movimientos;
        this.productos = productos;
        this.auditoria = auditoria;
    }

    public List<Movimiento> listar(Integer productoId, String tipo) {
        return movimientos.listar(productoId, tipo);
    }

    public Movimiento obtener(int id) {
        return movimientos.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("movimiento", id));
    }

    @Transactional
    public Movimiento registrar(MovimientoRequest peticion, int usuarioId) {
        Producto producto = productos.buscarPorId(peticion.productoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("producto", peticion.productoId()));

        int cantidad = peticion.cantidad();
        int stockResultante;

        switch (peticion.tipo()) {
            case "entrada" -> {
                productos.sumarStock(producto.id(), cantidad);
                stockResultante = producto.stockActual() + cantidad;
            }
            case "salida" -> {
                if (productos.descontarStock(producto.id(), cantidad) == 0) {
                    throw new ReglaNegocioException("Stock insuficiente de " + producto.codigo()
                            + " - " + producto.nombre() + ". Disponible: " + producto.stockActual()
                            + ", solicitado: " + cantidad);
                }
                stockResultante = producto.stockActual() - cantidad;
            }
            case "ajuste" -> {
                productos.fijarStock(producto.id(), cantidad);
                stockResultante = cantidad;
            }
            default -> throw new ReglaNegocioException(
                    "Tipo de movimiento no valido: " + peticion.tipo());
        }

        int id = movimientos.registrar(producto.id(), peticion.tipo(), cantidad, stockResultante,
                usuarioId, peticion.observacion());

        auditoria.registrar(usuarioId, "MOVIMIENTO_" + peticion.tipo().toUpperCase(), "movimientos",
                "Movimiento de " + peticion.tipo() + " de " + cantidad + " unidades de "
                        + producto.codigo() + ". Stock resultante: " + stockResultante);

        return obtener(id);
    }
}
