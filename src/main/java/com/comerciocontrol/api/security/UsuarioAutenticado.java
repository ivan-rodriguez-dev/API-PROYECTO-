package com.comerciocontrol.api.security;

/**
 * Datos del usuario que viajan dentro del token JWT y que quedan
 * disponibles en la peticion una vez validado.
 */
public record UsuarioAutenticado(Integer id, String usuario, String nombre, String rol) {

    /** Nombre del atributo con el que se guarda en la HttpServletRequest. */
    public static final String ATTR = "usuarioAutenticado";

    public boolean esAdministrador() {
        return "administrador".equals(rol);
    }
}
