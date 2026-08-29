package com.comerciocontrol.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Linea del carrito enviada al registrar una venta. */
public record ItemVentaRequest(

        @Schema(example = "1")
        @NotNull(message = "El id del producto es obligatorio")
        Integer productoId,

        @Schema(example = "3")
        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor que cero")
        Integer cantidad) {
}
