package com.comerciocontrol.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Movimiento de inventario solicitado por bodega. */
public record MovimientoRequest(

        @Schema(example = "2")
        @NotNull(message = "El id del producto es obligatorio")
        Integer productoId,

        @Schema(example = "entrada", allowableValues = {"entrada", "salida", "ajuste"})
        @NotBlank(message = "El tipo de movimiento es obligatorio")
        @Pattern(regexp = "entrada|salida|ajuste",
                message = "El tipo debe ser 'entrada', 'salida' o 'ajuste'")
        String tipo,

        @Schema(example = "20",
                description = "Unidades a ingresar o retirar. En un ajuste es el stock final deseado")
        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor que cero")
        Integer cantidad,

        @Schema(example = "Compra a Alimentos del Huila")
        @Size(max = 200, message = "La observacion no puede superar 200 caracteres")
        String observacion) {
}
