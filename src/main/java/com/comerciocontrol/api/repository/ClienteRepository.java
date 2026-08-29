package com.comerciocontrol.api.repository;

import com.comerciocontrol.api.dto.ClienteRequest;
import com.comerciocontrol.api.model.Cliente;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Acceso a datos de la tabla {@code clientes}. */
@Repository
public class ClienteRepository {

    private static final String COLUMNAS =
            "id, nombre, cedula, telefono, email, direccion, activo, fecha_registro";

    private static final RowMapper<Cliente> MAPPER = (rs, i) -> new Cliente(
            rs.getInt("id"), rs.getString("nombre"), rs.getString("cedula"),
            rs.getString("telefono"), rs.getString("email"), rs.getString("direccion"),
            rs.getInt("activo") == 1, rs.getString("fecha_registro"));

    private final JdbcTemplate jdbc;

    public ClienteRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Lista los clientes, opcionalmente filtrando por nombre o cedula. */
    public List<Cliente> listar(String busqueda) {
        if (busqueda == null || busqueda.isBlank()) {
            return jdbc.query("SELECT " + COLUMNAS + " FROM clientes ORDER BY nombre", MAPPER);
        }
        String patron = "%" + busqueda.trim().toLowerCase() + "%";
        return jdbc.query("SELECT " + COLUMNAS + " FROM clientes "
                        + "WHERE LOWER(nombre) LIKE ? OR IFNULL(cedula, '') LIKE ? ORDER BY nombre",
                MAPPER, patron, patron);
    }

    public Optional<Cliente> buscarPorId(int id) {
        return jdbc.query("SELECT " + COLUMNAS + " FROM clientes WHERE id = ?", MAPPER, id)
                .stream().findFirst();
    }

    public int crear(ClienteRequest r) {
        jdbc.update("INSERT INTO clientes (nombre, cedula, telefono, email, direccion, activo) "
                        + "VALUES (?,?,?,?,?,1)",
                r.nombre(), vacioANulo(r.cedula()), r.telefono(), r.email(), r.direccion());
        Integer id = jdbc.queryForObject("SELECT last_insert_rowid()", Integer.class);
        return id == null ? 0 : id;
    }

    public int actualizar(int id, ClienteRequest r) {
        return jdbc.update("UPDATE clientes SET nombre = ?, cedula = ?, telefono = ?, "
                        + "email = ?, direccion = ? WHERE id = ?",
                r.nombre(), vacioANulo(r.cedula()), r.telefono(), r.email(), r.direccion(), id);
    }

    /** Baja logica: conserva el historial de ventas asociado al cliente. */
    public int desactivar(int id) {
        return jdbc.update("UPDATE clientes SET activo = 0 WHERE id = ?", id);
    }

    public boolean existeCedula(String cedula, Integer idExcluido) {
        if (cedula == null || cedula.isBlank()) {
            return false;
        }
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM clientes WHERE cedula = ? AND id <> ?",
                Integer.class, cedula, idExcluido == null ? -1 : idExcluido);
        return n != null && n > 0;
    }

    public boolean existe(int id) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM clientes WHERE id = ?", Integer.class, id);
        return n != null && n > 0;
    }

    private String vacioANulo(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor;
    }
}
