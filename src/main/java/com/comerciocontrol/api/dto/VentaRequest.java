package com.comerciocontrol.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

/**
 * Venta que llega desde el punto de venta.
 * El total, el IVA y el subtotal los calcula el servidor: nunca se confia en el cliente.
 */
public record VentaRequest(

        @Schema(example = "2", description = "Opcional. Si se omite, la venta queda a consumidor final")
        Integer clienteId,

        @Schema(example = "0", description = "Descuento en pesos aplicado al subtotal")
        @PositiveOrZero(message = "El descuento no puede ser negativo")
        Double descuento,

        @NotEmpty(message = "La venta debe tener al menos un producto")
        @Valid
        List<ItemVentaRequest> items) {

    public double descuentoSeguro() {
        return descuento == null ? 0d : descuento;
    }
}
