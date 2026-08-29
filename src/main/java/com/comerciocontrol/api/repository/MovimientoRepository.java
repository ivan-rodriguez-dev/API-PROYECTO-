package com.comerciocontrol.api.repository;

import com.comerciocontrol.api.model.Movimiento;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acceso a datos de la tabla {@code movimientos} (kardex de inventario). */
@Repository
public class MovimientoRepository {

    private static final String SELECT_BASE = """
            SELECT m.id, m.producto_id, p.codigo, p.nombre, m.tipo, m.cantidad,
                   m.stock_resultante, m.fecha, m.usuario_id, u.nombre AS usuario_nombre,
                   m.observacion
            FROM movimientos m
            INNER JOIN productos p ON p.id = m.producto_id
            INNER JOIN usuarios  u ON u.id = m.usuario_id
            """;

    private static final RowMapper<Movimiento> MAPPER = (rs, i) -> new Movimiento(
            rs.getInt("id"), rs.getInt("producto_id"), rs.getString("codigo"),
            rs.getString("nombre"), rs.getString("tipo"), rs.getInt("cantidad"),
            rs.getInt("stock_resultante"), rs.getString("fecha"), rs.getInt("usuario_id"),
            rs.getString("usuario_nombre"), rs.getString("observacion"));

    private final JdbcTemplate jdbc;

    public MovimientoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Lista el kardex filtrando opcionalmente por producto y por tipo de movimiento. */
    public List<Movimiento> listar(Integer productoId, String tipo) {
        StringBuilder sql = new StringBuilder(SELECT_BASE).append(" WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        if (productoId != null) {
            sql.append(" AND m.producto_id = ?");
            params.add(productoId);
        }
        if (tipo != null && !tipo.isBlank()) {
            sql.append(" AND m.tipo = ?");
            params.add(tipo.trim().toLowerCase());
        }
        sql.append(" ORDER BY m.id DESC");
        return jdbc.query(sql.toString(), MAPPER, params.toArray());
    }

    public Optional<Movimiento> buscarPorId(int id) {
        return jdbc.query(SELECT_BASE + " WHERE m.id = ?", MAPPER, id).stream().findFirst();
    }

    public int registrar(int productoId, String tipo, int cantidad, int stockResultante,
                         int usuarioId, String observacion) {
        jdbc.update("INSERT INTO movimientos (producto_id, tipo, cantidad, stock_resultante, "
                        + "usuario_id, observacion) VALUES (?,?,?,?,?,?)",
                productoId, tipo, cantidad, stockResultante, usuarioId, observacion);
        Integer id = jdbc.queryForObject("SELECT last_insert_rowid()", Integer.class);
        return id == null ? 0 : id;
    }
}
