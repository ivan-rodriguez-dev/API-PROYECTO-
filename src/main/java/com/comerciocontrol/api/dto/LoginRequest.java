package com.comerciocontrol.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Credenciales enviadas a POST /api/auth/login. */
public record LoginRequest(

        @Schema(example = "ivan.admin")
        @NotBlank(message = "El usuario es obligatorio")
        @Size(max = 50, message = "El usuario no puede superar 50 caracteres")
        String usuario,

        @Schema(example = "Admin2026*")
        @NotBlank(message = "La contrasena es obligatoria")
        String password) {
}
