package com.comerciocontrol.api.exception;

/**
 * Una regla de negocio del proyecto impide completar la operacion
 * (stock insuficiente, caja cerrada, dato duplicado...). Se traduce a HTTP 409.
 */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
