package com.comerciocontrol.api.model;

/** Cliente del comercio. */
public record Cliente(Integer id, String nombre, String cedula, String telefono,
                      String email, String direccion, Boolean activo, String fechaRegistro) {
}
