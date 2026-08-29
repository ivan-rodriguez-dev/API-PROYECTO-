package com.comerciocontrol.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Alta de un usuario del sistema (solo administrador). */
public record UsuarioRequest(

        @Schema(example = "Sandra Milena Rojas")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        @Schema(example = "sandra.ventas")
        @NotBlank(message = "El usuario es obligatorio")
        @Size(min = 4, max = 50, message = "El usuario debe tener entre 4 y 50 caracteres")
        String usuario,

        @Schema(example = "Clave2026*")
        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres")
        String password,

        @Schema(example = "vendedor", allowableValues = {"administrador", "vendedor", "bodeguero"})
        @NotBlank(message = "El rol es obligatorio")
        @Pattern(regexp = "administrador|vendedor|bodeguero",
                message = "El rol debe ser 'administrador', 'vendedor' o 'bodeguero'")
        String rol) {
}
