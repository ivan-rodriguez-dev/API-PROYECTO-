package com.comerciocontrol.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Datos para crear o actualizar un producto del inventario. */
public record ProductoRequest(

        @Schema(example = "P-008")
        @NotBlank(message = "El codigo es obligatorio")
        @Size(max = 30, message = "El codigo no puede superar 30 caracteres")
        String codigo,

        @Schema(example = "Panela x 500 g")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        @Schema(example = "Abarrotes")
        @Size(max = 50, message = "La categoria no puede superar 50 caracteres")
        String categoria,

        @Schema(example = "2100")
        @NotNull(message = "El precio de costo es obligatorio")
        @PositiveOrZero(message = "El precio de costo no puede ser negativo")
        Double precioCosto,

        @Schema(example = "3200")
        @NotNull(message = "El precio de venta es obligatorio")
        @PositiveOrZero(message = "El precio de venta no puede ser negativo")
        Double precioVenta,

        @Schema(example = "50")
        @NotNull(message = "El stock actual es obligatorio")
        @PositiveOrZero(message = "El stock actual no puede ser negativo")
        Integer stockActual,

        @Schema(example = "10")
        @NotNull(message = "El stock minimo es obligatorio")
        @PositiveOrZero(message = "El stock minimo no puede ser negativo")
        Integer stockMinimo,

        @Schema(example = "2")
        Integer proveedorId) {
}
