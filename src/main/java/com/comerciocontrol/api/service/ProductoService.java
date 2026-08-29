package com.comerciocontrol.api.service;

import com.comerciocontrol.api.dto.ProductoRequest;
import com.comerciocontrol.api.exception.RecursoNoEncontradoException;
import com.comerciocontrol.api.exception.ReglaNegocioException;
import com.comerciocontrol.api.model.Producto;
import com.comerciocontrol.api.repository.ProductoRepository;
import com.comerciocontrol.api.repository.ProveedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Reglas de negocio del modulo de inventario. */
@Service
public class ProductoService {

    private final ProductoRepository productos;
    private final ProveedorRepository proveedores;
    private final AuditoriaService auditoria;

    public ProductoService(ProductoRepository productos, ProveedorRepository proveedores,
                           AuditoriaService auditoria) {
        this.productos = productos;
        this.proveedores = proveedores;
        this.auditoria = auditoria;
    }

    public List<Producto> listar(String q, String categoria, boolean incluirInactivos) {
        return productos.listar(q, categoria, !incluirInactivos);
    }

    public List<Producto> stockCritico() {
        return productos.listarStockCritico();
    }

    public List<String> categorias() {
        return productos.listarCategorias();
    }

    public Producto obtener(int id) {
        return productos.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("producto", id));
    }

    @Transactional
    public Producto crear(ProductoRequest peticion, int usuarioId) {
        validar(peticion, null);
        int id = productos.crear(peticion);
        auditoria.registrar(usuarioId, "CREAR_PRODUCTO", "productos",
                "Creo el producto " + peticion.codigo() + " - " + peticion.nombre());
        return obtener(id);
    }

    @Transactional
    public Producto actualizar(int id, ProductoRequest peticion, int usuarioId) {
        obtener(id);
        validar(peticion, id);
        productos.actualizar(id, peticion);
        auditoria.registrar(usuarioId, "ACTUALIZAR_PRODUCTO", "productos",
                "Actualizo el producto id " + id + " (" + peticion.codigo() + ")");
        return obtener(id);
    }

    /**
     * Da de baja el producto de forma logica.
     * No se borra fisicamente porque esta referenciado por ventas y movimientos historicos.
     */
    @Transactional
    public void desactivar(int id, int usuarioId) {
        Producto producto = obtener(id);
        productos.desactivar(id);
        auditoria.registrar(usuarioId, "DESACTIVAR_PRODUCTO", "productos",
                "Dio de baja el producto " + producto.codigo() + " - " + producto.nombre());
    }

    /** Validaciones de negocio que no puede expresar Bean Validation. */
    private void validar(ProductoRequest r, Integer idExcluido) {
        if (productos.existeCodigo(r.codigo(), idExcluido)) {
            throw new ReglaNegocioException("Ya existe un producto con el codigo '" + r.codigo() + "'");
        }
        if (r.precioVenta() < r.precioCosto()) {
            throw new ReglaNegocioException(
                    "El precio de venta (" + r.precioVenta() + ") no puede ser menor que el precio de costo ("
                            + r.precioCosto() + ")");
        }
        if (r.proveedorId() != null && !proveedores.existe(r.proveedorId())) {
            throw new RecursoNoEncontradoException("proveedor", r.proveedorId());
        }
    }
}
