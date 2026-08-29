package com.comerciocontrol.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Apertura de la jornada de caja. */
public record AperturaCajaRequest(

        @Schema(example = "100000", description = "Base en efectivo con la que abre la caja")
        @NotNull(message = "El monto de apertura es obligatorio")
        @PositiveOrZero(message = "El monto de apertura no puede ser negativo")
        Double apertura,

        @Schema(example = "Apertura del turno de la manana")
        @Size(max = 200, message = "La observacion no puede superar 200 caracteres")
        String observacion) {
}
