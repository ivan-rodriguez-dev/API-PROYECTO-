package com.comerciocontrol.api.repository;

import com.comerciocontrol.api.model.Auditoria;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/** Acceso a datos de la tabla {@code auditoria}. */
@Repository
public class AuditoriaRepository {

    private static final String SELECT_BASE = """
            SELECT a.id, a.usuario_id, u.nombre AS usuario_nombre, a.accion,
                   a.tabla_afectada, a.fecha, a.detalle
            FROM auditoria a
            INNER JOIN usuarios u ON u.id = a.usuario_id
            """;

    private static final RowMapper<Auditoria> MAPPER = (rs, i) -> new Auditoria(
            rs.getInt("id"), rs.getInt("usuario_id"), rs.getString("usuario_nombre"),
            rs.getString("accion"), rs.getString("tabla_afectada"), rs.getString("fecha"),
            rs.getString("detalle"));

    private final JdbcTemplate jdbc;

    public AuditoriaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Auditoria> listar(String tabla, Integer usuarioId, int limite) {
        StringBuilder sql = new StringBuilder(SELECT_BASE).append(" WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        if (tabla != null && !tabla.isBlank()) {
            sql.append(" AND a.tabla_afectada = ?");
            params.add(tabla.trim());
        }
        if (usuarioId != null) {
            sql.append(" AND a.usuario_id = ?");
            params.add(usuarioId);
        }
        sql.append(" ORDER BY a.id DESC LIMIT ?");
        params.add(limite);
        return jdbc.query(sql.toString(), MAPPER, params.toArray());
    }

    public void registrar(int usuarioId, String accion, String tablaAfectada, String detalle) {
        jdbc.update("INSERT INTO auditoria (usuario_id, accion, tabla_afectada, detalle) "
                + "VALUES (?,?,?,?)", usuarioId, accion, tablaAfectada, detalle);
    }
}
