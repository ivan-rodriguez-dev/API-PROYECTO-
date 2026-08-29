package com.comerciocontrol.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Datos para crear o actualizar un proveedor. */
public record ProveedorRequest(

        @Schema(example = "901222333-4")
        @NotBlank(message = "El NIT es obligatorio")
        @Size(max = 20, message = "El NIT no puede superar 20 caracteres")
        String nit,

        @Schema(example = "Comercializadora del Sur S.A.S.")
        @NotBlank(message = "La razon social es obligatoria")
        @Size(max = 120, message = "La razon social no puede superar 120 caracteres")
        String razonSocial,

        @Schema(example = "Andres Lopez")
        @Size(max = 100, message = "El contacto no puede superar 100 caracteres")
        String contacto,

        @Schema(example = "3181112233")
        @Size(max = 20, message = "El telefono no puede superar 20 caracteres")
        String telefono,

        @Schema(example = "compras@comersur.com")
        @Email(message = "El correo electronico no tiene un formato valido")
        String email) {
}
