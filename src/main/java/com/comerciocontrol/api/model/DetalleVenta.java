package com.comerciocontrol.api.model;

/** Linea de una venta: producto, cantidad y precio unitario aplicado. */
public record DetalleVenta(Integer id, Integer productoId, String productoCodigo,
                           String productoNombre, Integer cantidad,
                           Double precioUnitario, Double subtotal) {
}
