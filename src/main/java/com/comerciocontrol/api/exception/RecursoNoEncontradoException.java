package com.comerciocontrol.api.exception;

/** El recurso solicitado no existe en la base de datos. Se traduce a HTTP 404. */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String recurso, Object id) {
        super("No existe " + recurso + " con identificador " + id);
    }

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
