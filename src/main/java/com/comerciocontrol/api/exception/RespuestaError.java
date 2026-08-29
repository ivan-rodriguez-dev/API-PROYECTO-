package com.comerciocontrol.api.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Formato unico de error que devuelve la API.
 * Mantener una sola forma facilita el consumo desde el frontend y desde Postman.
 */
public record RespuestaError(
        LocalDateTime fecha,
        int estado,
        String error,
        String mensaje,
        String ruta,
        Map<String, String> detalles) {

    public static RespuestaError de(int estado, String error, String mensaje, String ruta) {
        return new RespuestaError(LocalDateTime.now(), estado, error, mensaje, ruta, null);
    }

    public static RespuestaError de(int estado, String error, String mensaje, String ruta,
                                    Map<String, String> detalles) {
        return new RespuestaError(LocalDateTime.now(), estado, error, mensaje, ruta, detalles);
    }
}
