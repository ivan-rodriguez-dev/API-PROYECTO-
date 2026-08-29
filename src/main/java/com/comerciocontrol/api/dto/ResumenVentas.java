package com.comerciocontrol.api.dto;

/** Reporte consolidado de ventas en un rango de fechas. */
public record ResumenVentas(String desde, String hasta, int cantidadVentas, double subtotal,
                            double descuentos, double iva, double total, double ticketPromedio) {
}
