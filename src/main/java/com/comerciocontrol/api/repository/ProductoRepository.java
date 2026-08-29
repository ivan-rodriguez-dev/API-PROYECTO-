package com.comerciocontrol.api.repository;

import com.comerciocontrol.api.dto.ProductoRequest;
import com.comerciocontrol.api.dto.ResumenInventario;
import com.comerciocontrol.api.model.Producto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acceso a datos de la tabla {@code productos}. */
@Repository
public class ProductoRepository {

    /** Consulta base: incluye la razon social del proveedor mediante LEFT JOIN. */
    private static final String SELECT_BASE = """
            SELECT p.id, p.codigo, p.nombre, p.categoria, p.precio_costo, p.precio_venta,
                   p.stock_actual, p.stock_minimo, p.activo, p.proveedor_id,
                   pr.razon_social AS proveedor_razon_social
            FROM productos p
            LEFT JOIN proveedores pr ON pr.id = p.proveedor_id
            """;

    private static final RowMapper<Producto> MAPPER = (rs, i) -> {
        int stockActual = rs.getInt("stock_actual");
        int stockMinimo = rs.getInt("stock_minimo");
        double costo = rs.getDouble("precio_costo");
        double venta = rs.getDouble("precio_venta");
        Integer proveedorId = rs.getObject("proveedor_id") == null ? null : rs.getInt("proveedor_id");
        return new Producto(
                rs.getInt("id"), rs.getString("codigo"), rs.getString("nombre"),
                rs.getString("categoria"), costo, venta, stockActual, stockMinimo,
                rs.getInt("activo") == 1, proveedorId, rs.getString("proveedor_razon_social"),
                stockActual <= stockMinimo ? "CRITICO" : "OK", venta - costo);
    };

    private final JdbcTemplate jdbc;

    public ProductoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Lista productos aplicando filtros opcionales.
     *
     * @param q         texto libre que se busca en el codigo o el nombre
     * @param categoria categoria exacta
     * @param soloActivos si es true excluye los productos dados de baja
     */
    public List<Producto> listar(String q, String categoria, boolean soloActivos) {
        StringBuilder sql = new StringBuilder(SELECT_BASE).append(" WHERE 1 = 1");
        List<Object> params = new ArrayList<>();

        if (soloActivos) {
            sql.append(" AND p.activo = 1");
        }
        if (q != null && !q.isBlank()) {
            sql.append(" AND (LOWER(p.nombre) LIKE ? OR LOWER(p.codigo) LIKE ?)");
            String patron = "%" + q.trim().toLowerCase() + "%";
            params.add(patron);
            params.add(patron);
        }
        if (categoria != null && !categoria.isBlank()) {
            sql.append(" AND LOWER(p.categoria) = ?");
            params.add(categoria.trim().toLowerCase());
        }
        sql.append(" ORDER BY p.nombre");
        return jdbc.query(sql.toString(), MAPPER, params.toArray());
    }

    /** Productos cuyo stock alcanzo o bajo del minimo definido. */
    public List<Producto> listarStockCritico() {
        return jdbc.query(SELECT_BASE
                + " WHERE p.activo = 1 AND p.stock_actual <= p.stock_minimo ORDER BY p.stock_actual", MAPPER);
    }

    public List<String> listarCategorias() {
        return jdbc.queryForList("SELECT DISTINCT categoria FROM productos "
                + "WHERE categoria IS NOT NULL AND categoria <> '' ORDER BY categoria", String.class);
    }

    public Optional<Producto> buscarPorId(int id) {
        return jdbc.query(SELECT_BASE + " WHERE p.id = ?", MAPPER, id).stream().findFirst();
    }

    public int crear(ProductoRequest r) {
        jdbc.update("INSERT INTO productos (codigo, nombre, categoria, precio_costo, precio_venta, "
                        + "stock_actual, stock_minimo, activo, proveedor_id) VALUES (?,?,?,?,?,?,?,1,?)",
                r.codigo(), r.nombre(), r.categoria(), r.precioCosto(), r.precioVenta(),
                r.stockActual(), r.stockMinimo(), r.proveedorId());
        Integer id = jdbc.queryForObject("SELECT last_insert_rowid()", Integer.class);
        return id == null ? 0 : id;
    }

    public int actualizar(int id, ProductoRequest r) {
        return jdbc.update("UPDATE productos SET codigo = ?, nombre = ?, categoria = ?, "
                        + "precio_costo = ?, precio_venta = ?, stock_actual = ?, stock_minimo = ?, "
                        + "proveedor_id = ? WHERE id = ?",
                r.codigo(), r.nombre(), r.categoria(), r.precioCosto(), r.precioVenta(),
                r.stockActual(), r.stockMinimo(), r.proveedorId(), id);
    }

    /** Baja logica: el producto deja de venderse pero conserva su historial. */
    public int desactivar(int id) {
        return jdbc.update("UPDATE productos SET activo = 0 WHERE id = ?", id);
    }

    /**
     * Descuenta stock solo si hay existencias suficientes.
     * La condicion viaja dentro del UPDATE para que la verificacion sea atomica.
     *
     * @return numero de filas afectadas: 0 significa stock insuficiente
     */
    public int descontarStock(int productoId, int cantidad) {
        return jdbc.update("UPDATE productos SET stock_actual = stock_actual - ? "
                + "WHERE id = ? AND stock_actual >= ?", cantidad, productoId, cantidad);
    }

    public int sumarStock(int productoId, int cantidad) {
        return jdbc.update("UPDATE productos SET stock_actual = stock_actual + ? WHERE id = ?",
                cantidad, productoId);
    }

    public int fijarStock(int productoId, int nuevoStock) {
        return jdbc.update("UPDATE productos SET stock_actual = ? WHERE id = ?", nuevoStock, productoId);
    }

    public boolean existeCodigo(String codigo, Integer idExcluido) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM productos WHERE codigo = ? AND id <> ?",
                Integer.class, codigo, idExcluido == null ? -1 : idExcluido);
        return n != null && n > 0;
    }

    /** Consolidado del inventario para el modulo de reportes. */
    public ResumenInventario resumenInventario() {
        return jdbc.queryForObject("""
                SELECT COUNT(*)                                  AS total_productos,
                       IFNULL(SUM(stock_actual), 0)              AS unidades,
                       IFNULL(SUM(stock_actual * precio_costo), 0) AS valor_costo,
                       IFNULL(SUM(stock_actual * precio_venta), 0) AS valor_venta,
                       IFNULL(SUM(CASE WHEN stock_actual <= stock_minimo THEN 1 ELSE 0 END), 0) AS criticos
                FROM productos WHERE activo = 1
                """, (rs, i) -> new ResumenInventario(
                rs.getInt("total_productos"), rs.getInt("unidades"),
                rs.getDouble("valor_costo"), rs.getDouble("valor_venta"),
                rs.getDouble("valor_venta") - rs.getDouble("valor_costo"),
                rs.getInt("criticos")));
    }
}
