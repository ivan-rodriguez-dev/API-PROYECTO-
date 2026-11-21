package com.comerciocontrol.api.config;

import com.comerciocontrol.api.repository.UsuarioRepository;
import com.comerciocontrol.api.security.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Crea el primer administrador en el perfil de implantacion, donde no se cargan
 * usuarios de prueba. Los datos llegan por variables de entorno para que ninguna
 * credencial quede en el repositorio.
 */
@Component
@Profile("prod")
public class AdministradorInicial implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(AdministradorInicial.class);
    private static final int LONGITUD_MINIMA = 12;

    private final UsuarioRepository usuarios;
    private final PasswordHasher hasher;
    private final String usuario;
    private final String nombre;
    private final String password;

    public AdministradorInicial(UsuarioRepository usuarios, PasswordHasher hasher,
                                @Value("${app.admin.usuario}") String usuario,
                                @Value("${app.admin.nombre}") String nombre,
                                @Value("${app.admin.password}") String password) {
        this.usuarios = usuarios;
        this.hasher = hasher;
        this.usuario = usuario;
        this.nombre = nombre;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (usuarios.existeAdministrador()) {
            return;
        }
        if (usuario.isBlank() || password.length() < LONGITUD_MINIMA) {
            throw new IllegalStateException("Defina APP_ADMIN_USUARIO y APP_ADMIN_PASSWORD (minimo "
                    + LONGITUD_MINIMA + " caracteres) para crear el primer administrador");
        }
        usuarios.crear(nombre, usuario, hasher.hash(password), "administrador");
        LOG.info("Administrador inicial creado: {}", usuario);
    }
}
