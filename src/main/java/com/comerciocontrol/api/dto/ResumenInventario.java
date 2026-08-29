package com.comerciocontrol.api.dto;

/** Reporte consolidado del estado del inventario. */
public record ResumenInventario(int totalProductos, int unidadesTotales, double valorCosto,
                                double valorVenta, double utilidadPotencial, int productosCriticos) {
}
