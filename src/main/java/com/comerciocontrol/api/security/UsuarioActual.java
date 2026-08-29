package com.comerciocontrol.api.security;

import com.comerciocontrol.api.exception.NoAutenticadoException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/** Utilidad para obtener, dentro de un servicio o controlador, el usuario de la peticion. */
@Component
public class UsuarioActual {

    private final HttpServletRequest request;

    public UsuarioActual(HttpServletRequest request) {
        this.request = request;
    }

    public UsuarioAutenticado obtener() {
        Object u = request.getAttribute(UsuarioAutenticado.ATTR);
        if (u == null) {
            throw new NoAutenticadoException("No hay un usuario autenticado en la peticion");
        }
        return (UsuarioAutenticado) u;
    }

    public Integer id() {
        return obtener().id();
    }
}
