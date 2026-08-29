package com.comerciocontrol.api.dto;

/** Fila del reporte de productos mas vendidos. */
public record ProductoVendido(Integer productoId, String codigo, String nombre,
                              int unidadesVendidas, double totalVendido) {
}
