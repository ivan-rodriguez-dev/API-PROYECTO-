package com.comerciocontrol.api.exception;

/** El usuario esta autenticado pero su rol no tiene permiso. Se traduce a HTTP 403. */
public class AccesoDenegadoException extends RuntimeException {
    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
