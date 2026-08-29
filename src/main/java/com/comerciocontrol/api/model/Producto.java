package com.comerciocontrol.api.model;

/**
 * Producto del inventario.
 *
 * @param estado           calculado: "CRITICO" si el stock actual esta en el minimo o por debajo, "OK" en caso contrario
 * @param utilidadUnitaria calculada: precio de venta menos precio de costo
 */
public record Producto(Integer id, String codigo, String nombre, String categoria,
                       Double precioCosto, Double precioVenta, Integer stockActual,
                       Integer stockMinimo, Boolean activo, Integer proveedorId,
                       String proveedorRazonSocial, String estado, Double utilidadUnitaria) {
}
