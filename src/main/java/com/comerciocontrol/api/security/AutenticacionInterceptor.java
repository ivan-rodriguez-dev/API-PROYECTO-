package com.comerciocontrol.api.security;

import com.comerciocontrol.api.exception.AccesoDenegadoException;
import com.comerciocontrol.api.exception.NoAutenticadoException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

/**
 * Verifica el token JWT de cada peticion y aplica el control de acceso por rol
 * declarado con {@link RolesPermitidos}.
 *
 * <p>Reproduce en la API la misma matriz de permisos de la aplicacion de escritorio:
 * administrador (todo), vendedor (ventas, clientes y caja) y bodeguero
 * (inventario, movimientos y proveedores).</p>
 */
@Component
public class AutenticacionInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;

    public AutenticacionInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Las peticiones CORS de sondeo no llevan token.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || !(handler instanceof HandlerMethod metodo)) {
            return true;
        }

        String cabecera = request.getHeader("Authorization");
        if (cabecera == null || !cabecera.startsWith("Bearer ")) {
            throw new NoAutenticadoException(
                    "Falta la cabecera Authorization. Inicie sesion en POST /api/auth/login "
                            + "y envie el token como 'Bearer <token>'");
        }

        UsuarioAutenticado usuario = jwtService.validar(cabecera.substring(7).trim());
        if (usuario == null) {
            throw new NoAutenticadoException("El token es invalido o ya expiro. Vuelva a iniciar sesion");
        }
        request.setAttribute(UsuarioAutenticado.ATTR, usuario);

        RolesPermitidos permiso = metodo.getMethodAnnotation(RolesPermitidos.class);
        if (permiso == null) {
            permiso = metodo.getBeanType().getAnnotation(RolesPermitidos.class);
        }
        if (permiso != null && Arrays.stream(permiso.value()).noneMatch(r -> r.equals(usuario.rol()))) {
            throw new AccesoDenegadoException("El rol '" + usuario.rol()
                    + "' no tiene permiso para esta operacion. Roles autorizados: "
                    + String.join(", ", permiso.value()));
        }
        return true;
    }
}
