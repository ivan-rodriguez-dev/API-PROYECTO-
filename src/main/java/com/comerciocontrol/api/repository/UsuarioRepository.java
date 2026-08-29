package com.comerciocontrol.api.repository;

import com.comerciocontrol.api.model.Usuario;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Acceso a datos de la tabla {@code usuarios}. */
@Repository
public class UsuarioRepository {

    private static final RowMapper<Usuario> MAPPER = (rs, i) -> new Usuario(
            rs.getInt("id"), rs.getString("nombre"), rs.getString("usuario"),
            rs.getString("rol"), rs.getInt("activo") == 1);

    private final JdbcTemplate jdbc;

    public UsuarioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Usuario> listar() {
        return jdbc.query("SELECT id, nombre, usuario, rol, activo FROM usuarios ORDER BY nombre", MAPPER);
    }

    public Optional<Usuario> buscarPorId(int id) {
        return jdbc.query("SELECT id, nombre, usuario, rol, activo FROM usuarios WHERE id = ?",
                MAPPER, id).stream().findFirst();
    }

    public Optional<Usuario> buscarPorUsuario(String usuario) {
        return jdbc.query("SELECT id, nombre, usuario, rol, activo FROM usuarios WHERE usuario = ?",
                MAPPER, usuario).stream().findFirst();
    }

    /** Devuelve el hash almacenado para validar el inicio de sesion. */
    public Optional<String> buscarHash(String usuario) {
        return jdbc.query("SELECT password_hash FROM usuarios WHERE usuario = ? AND activo = 1",
                (rs, i) -> rs.getString(1), usuario).stream().findFirst();
    }

    public int crear(String nombre, String usuario, String passwordHash, String rol) {
        jdbc.update("INSERT INTO usuarios (nombre, usuario, password_hash, rol, activo) VALUES (?,?,?,?,1)",
                nombre, usuario, passwordHash, rol);
        return ultimoId();
    }

    public int cambiarEstado(int id, boolean activo) {
        return jdbc.update("UPDATE usuarios SET activo = ? WHERE id = ?", activo ? 1 : 0, id);
    }

    public boolean existeUsuario(String usuario) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM usuarios WHERE usuario = ?",
                Integer.class, usuario);
        return n != null && n > 0;
    }

    private int ultimoId() {
        Integer id = jdbc.queryForObject("SELECT last_insert_rowid()", Integer.class);
        return id == null ? 0 : id;
    }
}
