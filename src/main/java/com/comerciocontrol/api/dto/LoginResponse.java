package com.comerciocontrol.api.dto;

import com.comerciocontrol.api.model.Usuario;

/** Respuesta del login: token JWT y datos del usuario autenticado. */
public record LoginResponse(String token, String tipo, long expiraEnSegundos, Usuario usuario) {

    public static LoginResponse de(String token, long expiraEnSegundos, Usuario usuario) {
        return new LoginResponse(token, "Bearer", expiraEnSegundos, usuario);
    }
}
