package com.comerciocontrol.api.repository;

import com.comerciocontrol.api.model.Caja;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Acceso a datos de la tabla {@code caja} (jornadas de caja diaria). */
@Repository
public class CajaRepository {

    private static final String SELECT_BASE = """
            SELECT c.id, c.fecha, c.apertura, c.cierre, c.ventas_dia, c.diferencia,
                   c.observacion, c.usuario_id, u.nombre AS usuario_nombre, c.estado
            FROM caja c
            INNER JOIN usuarios u ON u.id = c.usuario_id
            """;

    private static final RowMapper<Caja> MAPPER = (rs, i) -> new Caja(
            rs.getInt("id"), rs.getString("fecha"), rs.getDouble("apertura"),
            rs.getDouble("cierre"), rs.getDouble("ventas_dia"), rs.getDouble("diferencia"),
            rs.getString("observacion"), rs.getInt("usuario_id"), rs.getString("usuario_nombre"),
            rs.getString("estado"));

    private final JdbcTemplate jdbc;

    public CajaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Caja abierta actualmente, si existe. Solo puede haber una a la vez. */
    public Optional<Caja> buscarAbierta() {
        return jdbc.query(SELECT_BASE + " WHERE c.estado = 'abierta' ORDER BY c.id DESC LIMIT 1", MAPPER)
                .stream().findFirst();
    }

    public Optional<Caja> buscarPorId(int id) {
        return jdbc.query(SELECT_BASE + " WHERE c.id = ?", MAPPER, id).stream().findFirst();
    }

    public List<Caja> listar() {
        return jdbc.query(SELECT_BASE + " ORDER BY c.id DESC", MAPPER);
    }

    public int abrir(String fecha, double apertura, int usuarioId, String observacion) {
        jdbc.update("INSERT INTO caja (fecha, apertura, cierre, ventas_dia, diferencia, "
                        + "observacion, usuario_id, estado) VALUES (?,?,0,0,0,?,?,'abierta')",
                fecha, apertura, observacion, usuarioId);
        Integer id = jdbc.queryForObject("SELECT last_insert_rowid()", Integer.class);
        return id == null ? 0 : id;
    }

    public int cerrar(int id, double efectivoContado, double ventasDia, double diferencia,
                      String observacion) {
        return jdbc.update("UPDATE caja SET cierre = ?, ventas_dia = ?, diferencia = ?, "
                        + "observacion = ?, estado = 'cerrada' WHERE id = ? AND estado = 'abierta'",
                efectivoContado, ventasDia, diferencia, observacion, id);
    }

    /** Suma de las ventas registradas contra una caja concreta. */
    public double totalVentasDeCaja(int cajaId) {
        Double total = jdbc.queryForObject(
                "SELECT IFNULL(SUM(total), 0) FROM ventas WHERE caja_id = ?", Double.class, cajaId);
        return total == null ? 0d : total;
    }
}
