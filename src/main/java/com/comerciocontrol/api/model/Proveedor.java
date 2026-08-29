package com.comerciocontrol.api.model;

/** Proveedor que surte los productos del inventario. */
public record Proveedor(Integer id, String nit, String razonSocial, String contacto,
                        String telefono, String email, Boolean activo) {
}
