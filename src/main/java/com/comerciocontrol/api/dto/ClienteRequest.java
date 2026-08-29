package com.comerciocontrol.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos para crear o actualizar un cliente. */
public record ClienteRequest(

        @Schema(example = "Sofia Ramirez Leon")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        @Schema(example = "1075004004")
        @Pattern(regexp = "^$|^[0-9]{6,20}$", message = "La cedula debe tener entre 6 y 20 digitos")
        String cedula,

        @Schema(example = "3009998877")
        @Pattern(regexp = "^$|^[0-9+ -]{7,20}$", message = "El telefono no tiene un formato valido")
        String telefono,

        @Schema(example = "sofia.ramirez@mail.com")
        @Email(message = "El correo electronico no tiene un formato valido")
        @Size(max = 100, message = "El correo no puede superar 100 caracteres")
        String email,

        @Schema(example = "Cll 12 # 7-30")
        @Size(max = 150, message = "La direccion no puede superar 150 caracteres")
        String direccion) {
}
