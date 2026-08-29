package com.comerciocontrol.api.repository;

import com.comerciocontrol.api.dto.ProductoVendido;
import com.comerciocontrol.api.dto.ResumenVentas;
import com.comerciocontrol.api.model.DetalleVenta;
import com.comerciocontrol.api.model.Venta;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Acceso a datos de las tablas {@code ventas} y {@code detalle_ventas}. */
@Repository
public class VentaRepository {

    private static final String SELECT_BASE = """
            SELECT v.id, v.fecha, v.subtotal, v.descuento, v.iva, v.total,
                   v.usuario_id, u.nombre AS usuario_nombre,
                   v.cliente_id, c.nombre AS cliente_nombre, v.caja_id
            FROM ventas v
            INNER JOIN usuarios u ON u.id = v.usuario_id
            LEFT  JOIN clientes c ON c.id = v.cliente_id
            """;

    private static final RowMapper<Venta> MAPPER = (rs, i) -> new Venta(
            rs.getInt("id"), rs.getString("fecha"), rs.getDouble("subtotal"),
            rs.getDouble("descuento"), rs.getDouble("iva"), rs.getDouble("total"),
            rs.getInt("usuario_id"), rs.getString("usuario_nombre"),
            rs.getObject("cliente_id") == null ? null : rs.getInt("cliente_id"),
            rs.getString("cliente_nombre"),
            rs.getObject("caja_id") == null ? null : rs.getInt("caja_id"),
            null);

    private static final RowMapper<DetalleVenta> MAPPER_DETALLE = (rs, i) -> new DetalleVenta(
            rs.getInt("id"), rs.getInt("producto_id"), rs.getString("codigo"),
            rs.getString("nombre"), rs.getInt("cantidad"), rs.getDouble("precio_unitario"),
            rs.getInt("cantidad") * rs.getDouble("precio_unitario"));

    private final JdbcTemplate jdbc;

    public VentaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Lista las ventas del rango indicado (ambas fechas opcionales, formato {@code yyyy-MM-dd}).
     * Las cabeceras se devuelven sin detalle para que el listado sea liviano.
     */
    public List<Venta> listar(String desde, String hasta) {
        String sql = SELECT_BASE + " WHERE DATE(v.fecha) BETWEEN ? AND ? ORDER BY v.id DESC";
        return jdbc.query(sql, MAPPER, desde, hasta);
    }

    public Optional<Venta> buscarPorId(int id) {
        Optional<Venta> venta = jdbc.query(SELECT_BASE + " WHERE v.id = ?", MAPPER, id)
                .stream().findFirst();
        return venta.map(v -> new Venta(v.id(), v.fecha(), v.subtotal(), v.descuento(), v.iva(),
                v.total(), v.usuarioId(), v.usuarioNombre(), v.clienteId(), v.clienteNombre(),
                v.cajaId(), listarDetalle(id)));
    }

    public List<DetalleVenta> listarDetalle(int ventaId) {
        return jdbc.query("""
                SELECT d.id, d.producto_id, p.codigo, p.nombre, d.cantidad, d.precio_unitario
                FROM detalle_ventas d
                INNER JOIN productos p ON p.id = d.producto_id
                WHERE d.venta_id = ?
                ORDER BY d.id
                """, MAPPER_DETALLE, ventaId);
    }

    public int crearCabecera(double subtotal, double descuento, double iva, double total,
                             int usuarioId, Integer clienteId, Integer cajaId) {
        jdbc.update("INSERT INTO ventas (subtotal, descuento, iva, total, usuario_id, cliente_id, caja_id) "
                        + "VALUES (?,?,?,?,?,?,?)",
                subtotal, descuento, iva, total, usuarioId, clienteId, cajaId);
        Integer id = jdbc.queryForObject("SELECT last_insert_rowid()", Integer.class);
        return id == null ? 0 : id;
    }

    public void agregarDetalle(int ventaId, int productoId, int cantidad, double precioUnitario) {
        jdbc.update("INSERT INTO detalle_ventas (venta_id, producto_id, cantidad, precio_unitario) "
                + "VALUES (?,?,?,?)", ventaId, productoId, cantidad, precioUnitario);
    }

    /** Reporte consolidado de ventas entre dos fechas. */
    public ResumenVentas resumen(String desde, String hasta) {
        return jdbc.queryForObject("""
                SELECT COUNT(*)                      AS cantidad,
                       IFNULL(SUM(subtotal), 0)      AS subtotal,
                       IFNULL(SUM(descuento), 0)     AS descuentos,
                       IFNULL(SUM(iva), 0)           AS iva,
                       IFNULL(SUM(total), 0)         AS total
                FROM ventas WHERE DATE(fecha) BETWEEN ? AND ?
                """, (rs, i) -> {
            int cantidad = rs.getInt("cantidad");
            double total = rs.getDouble("total");
            return new ResumenVentas(desde, hasta, cantidad, rs.getDouble("subtotal"),
                    rs.getDouble("descuentos"), rs.getDouble("iva"), total,
                    cantidad == 0 ? 0 : Math.round((total / cantidad) * 100.0) / 100.0);
        }, desde, hasta);
    }

    /** Ranking de productos mas vendidos en el rango indicado. */
    public List<ProductoVendido> masVendidos(String desde, String hasta, int limite) {
        return jdbc.query("""
                SELECT p.id, p.codigo, p.nombre,
                       SUM(d.cantidad)                        AS unidades,
                       SUM(d.cantidad * d.precio_unitario)    AS total_vendido
                FROM detalle_ventas d
                INNER JOIN ventas v    ON v.id = d.venta_id
                INNER JOIN productos p ON p.id = d.producto_id
                WHERE DATE(v.fecha) BETWEEN ? AND ?
                GROUP BY p.id, p.codigo, p.nombre
                ORDER BY unidades DESC, total_vendido DESC
                LIMIT ?
                """, (rs, i) -> new ProductoVendido(rs.getInt("id"), rs.getString("codigo"),
                rs.getString("nombre"), rs.getInt("unidades"), rs.getDouble("total_vendido")),
                desde, hasta, limite);
    }
}
