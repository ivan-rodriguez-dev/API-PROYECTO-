package com.comerciocontrol.api.service;

import com.comerciocontrol.api.dto.LoginRequest;
import com.comerciocontrol.api.dto.LoginResponse;
import com.comerciocontrol.api.dto.UsuarioRequest;
import com.comerciocontrol.api.exception.NoAutenticadoException;
import com.comerciocontrol.api.exception.RecursoNoEncontradoException;
import com.comerciocontrol.api.exception.ReglaNegocioException;
import com.comerciocontrol.api.model.Usuario;
import com.comerciocontrol.api.repository.UsuarioRepository;
import com.comerciocontrol.api.security.JwtService;
import com.comerciocontrol.api.security.PasswordHasher;
import com.comerciocontrol.api.security.UsuarioAutenticado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Autenticacion de usuarios y administracion de cuentas. */
@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final PasswordHasher hasher;
    private final JwtService jwt;
    private final AuditoriaService auditoria;

    public AuthService(UsuarioRepository usuarios, PasswordHasher hasher,
                       JwtService jwt, AuditoriaService auditoria) {
        this.usuarios = usuarios;
        this.hasher = hasher;
        this.jwt = jwt;
        this.auditoria = auditoria;
    }

    /**
     * Valida las credenciales y emite el token JWT.
     *
     * <p>El mensaje de error es deliberadamente generico: no revela si lo que
     * fallo fue el usuario o la contrasena.</p>
     */
    @Transactional
    public LoginResponse login(LoginRequest peticion) {
        String hashAlmacenado = usuarios.buscarHash(peticion.usuario())
                .orElseThrow(() -> new NoAutenticadoException("Usuario o contrasena incorrectos"));

        if (!hasher.coincide(peticion.password(), hashAlmacenado)) {
            throw new NoAutenticadoException("Usuario o contrasena incorrectos");
        }

        Usuario usuario = usuarios.buscarPorUsuario(peticion.usuario())
                .orElseThrow(() -> new NoAutenticadoException("Usuario o contrasena incorrectos"));

        String token = jwt.generar(new UsuarioAutenticado(
                usuario.id(), usuario.usuario(), usuario.nombre(), usuario.rol()));

        auditoria.registrar(usuario.id(), "INICIO_SESION", "usuarios",
                "Inicio de sesion del usuario " + usuario.usuario());

        return LoginResponse.de(token, jwt.getExpiracionSegundos(), usuario);
    }

    public Usuario perfil(int usuarioId) {
        return usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("usuario", usuarioId));
    }

    public List<Usuario> listar() {
        return usuarios.listar();
    }

    @Transactional
    public Usuario crear(UsuarioRequest peticion, int usuarioResponsable) {
        if (usuarios.existeUsuario(peticion.usuario())) {
            throw new ReglaNegocioException(
                    "Ya existe un usuario con el nombre de cuenta '" + peticion.usuario() + "'");
        }
        int id = usuarios.crear(peticion.nombre(), peticion.usuario(),
                hasher.hash(peticion.password()), peticion.rol());

        auditoria.registrar(usuarioResponsable, "CREAR_USUARIO", "usuarios",
                "Creo el usuario " + peticion.usuario() + " con rol " + peticion.rol());

        return usuarios.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("usuario", id));
    }

    @Transactional
    public Usuario cambiarEstado(int id, boolean activo, int usuarioResponsable) {
        if (usuarios.cambiarEstado(id, activo) == 0) {
            throw new RecursoNoEncontradoException("usuario", id);
        }
        auditoria.registrar(usuarioResponsable, activo ? "ACTIVAR_USUARIO" : "DESACTIVAR_USUARIO",
                "usuarios", "Cambio el estado del usuario id " + id);
        return usuarios.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("usuario", id));
    }
}
