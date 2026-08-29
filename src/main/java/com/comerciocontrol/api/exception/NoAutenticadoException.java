package com.comerciocontrol.api.exception;

/** Falta el token o es invalido. Se traduce a HTTP 401. */
public class NoAutenticadoException extends RuntimeException {
    public NoAutenticadoException(String mensaje) {
        super(mensaje);
    }
}
