package com.comerciocontrol.api.repository;

import com.comerciocontrol.api.dto.ProveedorRequest;
import com.comerciocontrol.api.model.Proveedor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Acceso a datos de la tabla {@code proveedores}. */
@Repository
public class ProveedorRepository {

    private static final String COLUMNAS = "id, nit, razon_social, contacto, telefono, email, activo";

    private static final RowMapper<Proveedor> MAPPER = (rs, i) -> new Proveedor(
            rs.getInt("id"), rs.getString("nit"), rs.getString("razon_social"),
            rs.getString("contacto"), rs.getString("telefono"), rs.getString("email"),
            rs.getInt("activo") == 1);

    private final JdbcTemplate jdbc;

    public ProveedorRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Proveedor> listar() {
        return jdbc.query("SELECT " + COLUMNAS + " FROM proveedores ORDER BY razon_social", MAPPER);
    }

    public Optional<Proveedor> buscarPorId(int id) {
        return jdbc.query("SELECT " + COLUMNAS + " FROM proveedores WHERE id = ?", MAPPER, id)
                .stream().findFirst();
    }

    public int crear(ProveedorRequest r) {
        jdbc.update("INSERT INTO proveedores (nit, razon_social, contacto, telefono, email, activo) "
                        + "VALUES (?,?,?,?,?,1)",
                r.nit(), r.razonSocial(), r.contacto(), r.telefono(), r.email());
        Integer id = jdbc.queryForObject("SELECT last_insert_rowid()", Integer.class);
        return id == null ? 0 : id;
    }

    public int actualizar(int id, ProveedorRequest r) {
        return jdbc.update("UPDATE proveedores SET nit = ?, razon_social = ?, contacto = ?, "
                        + "telefono = ?, email = ? WHERE id = ?",
                r.nit(), r.razonSocial(), r.contacto(), r.telefono(), r.email(), id);
    }

    public int desactivar(int id) {
        return jdbc.update("UPDATE proveedores SET activo = 0 WHERE id = ?", id);
    }

    public boolean existeNit(String nit, Integer idExcluido) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM proveedores WHERE nit = ? AND id <> ?",
                Integer.class, nit, idExcluido == null ? -1 : idExcluido);
        return n != null && n > 0;
    }

    public boolean existe(int id) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM proveedores WHERE id = ?", Integer.class, id);
        return n != null && n > 0;
    }
}
