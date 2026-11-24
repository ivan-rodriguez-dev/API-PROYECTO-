package com.comerciocontrol.api.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Copia de seguridad y verificacion de integridad de la base de datos SQLite.
 *
 * <p>La copia se genera con {@code VACUUM INTO}: SQLite escribe una base nueva,
 * compacta y consistente aunque la API siga atendiendo peticiones, sin detener el
 * servicio. El archivo temporal se borra apenas se lee.</p>
 */
@Service
public class RespaldoService {

    /** Tablas del esquema que se cuentan al verificar una base restaurada. */
    private static final List<String> TABLAS = List.of(
            "usuarios", "clientes", "proveedores", "productos", "caja",
            "ventas", "detalle_ventas", "movimientos", "auditoria");

    private final JdbcTemplate jdbc;

    public RespaldoService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Genera una copia consistente de la base de datos y devuelve sus bytes. */
    public byte[] generarCopia() {
        Path carpeta = null;
        Path destino = null;
        try {
            carpeta = Files.createTempDirectory("comerciocontrol-respaldo");
            destino = carpeta.resolve("comerciocontrol.db");
            // La ruta la genera la API; nunca viene del usuario.
            jdbc.execute("VACUUM INTO '" + destino.toString().replace("'", "''") + "'");
            return Files.readAllBytes(destino);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar la copia de seguridad", e);
        } finally {
            borrar(destino);
            borrar(carpeta);
        }
    }

    /** Integridad de la base y cantidad de registros por tabla. */
    public Map<String, Object> verificar() {
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("fecha", LocalDateTime.now().withNano(0).toString());
        resultado.put("integridad", jdbc.queryForObject("PRAGMA integrity_check", String.class));
        resultado.put("versionSqlite", jdbc.queryForObject("SELECT sqlite_version()", String.class));
        Map<String, Integer> tablas = new LinkedHashMap<>();
        for (String tabla : TABLAS) {
            tablas.put(tabla, jdbc.queryForObject("SELECT COUNT(*) FROM " + tabla, Integer.class));
        }
        resultado.put("tablas", tablas);
        return resultado;
    }

    private static void borrar(Path ruta) {
        if (ruta == null) {
            return;
        }
        try {
            Files.deleteIfExists(ruta);
        } catch (IOException ignorada) {
            // El sistema operativo limpia la carpeta temporal si el borrado falla.
        }
    }
}
