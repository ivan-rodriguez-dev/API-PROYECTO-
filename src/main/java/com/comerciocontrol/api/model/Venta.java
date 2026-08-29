package com.comerciocontrol.api.model;

import java.util.List;

/** Venta registrada en el punto de venta, con sus lineas de detalle. */
public record Venta(Integer id, String fecha, Double subtotal, Double descuento, Double iva,
                    Double total, Integer usuarioId, String usuarioNombre,
                    Integer clienteId, String clienteNombre, Integer cajaId,
                    List<DetalleVenta> detalles) {
}
