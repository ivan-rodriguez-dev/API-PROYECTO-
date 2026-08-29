package com.comerciocontrol.api.model;

/** Usuario del sistema. El hash de la contrasena nunca se expone en las respuestas. */
public record Usuario(Integer id, String nombre, String usuario, String rol, Boolean activo) {
}
