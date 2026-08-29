package com.comerciocontrol.api.model;

/**
 * Jornada de caja.
 *
 * <p>La diferencia se calcula al cerrar como
 * {@code efectivo contado - (apertura + ventas del dia)}.</p>
 */
public record Caja(Integer id, String fecha, Double apertura, Double cierre, Double ventasDia,
                   Double diferencia, String observacion, Integer usuarioId,
                   String usuarioNombre, String estado) {
}
