package com.comerciocontrol.api.model;

/** Movimiento de stock: entrada, salida o ajuste de inventario. */
public record Movimiento(Integer id, Integer productoId, String productoCodigo,
                         String productoNombre, String tipo, Integer cantidad,
                         Integer stockResultante, String fecha, Integer usuarioId,
                         String usuarioNombre, String observacion) {
}
