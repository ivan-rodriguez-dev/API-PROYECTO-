package com.comerciocontrol.api.model;

/** Registro de auditoria: quien hizo que, sobre que tabla y cuando. */
public record Auditoria(Integer id, Integer usuarioId, String usuarioNombre, String accion,
                        String tablaAfectada, String fecha, String detalle) {
}
