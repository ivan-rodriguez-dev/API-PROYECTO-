package com.comerciocontrol.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Cierre de la jornada de caja con el efectivo realmente contado. */
public record CierreCajaRequest(

        @Schema(example = "235938", description = "Efectivo contado fisicamente al cerrar")
        @NotNull(message = "El efectivo contado es obligatorio")
        @PositiveOrZero(message = "El efectivo contado no puede ser negativo")
        Double efectivoContado,

        @Schema(example = "Cierre sin novedades")
        @Size(max = 200, message = "La observacion no puede superar 200 caracteres")
        String observacion) {
}
