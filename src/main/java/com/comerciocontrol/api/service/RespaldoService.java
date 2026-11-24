package com.comerciocontrol.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.sqlite.SQLiteConfig;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Copia de seguridad, restauracion y verificacion de integridad de la base SQLite.
 *
 * <p>La copia se genera con {@code VACUUM INTO}: SQLite escribe una base nueva,
 * compacta y consistente aunque la API siga atendiendo peticiones, sin detener el
 * servicio. El archivo temporal se borra apenas se lee.</p>
 *
 * <p>La restauracion usa la API de respaldo en linea de SQLite ({@code restore from}),
 * que copia la base pagina por pagina sobre la conexion abierta. Asi funciona en
 * plataformas donde no hay acceso al disco del servidor, como Railway.</p>
 */
@Service
public class RespaldoService {

    /** Tablas del esquema que se cuentan al verificar una base restaurada. */
    private static final List<String> TABLAS = List.of(
            "usuarios", "clientes", "proveedores", "productos", "caja",
            "ventas", "detalle_ventas", "movimientos", "auditoria");

    /** Todo archivo SQLite 3 empieza con estos 16 bytes. */
    private static final byte[] CABECERA_SQLITE = "SQLite format 3\0".getBytes(StandardCharsets.US_ASCII);

    /** Copias automaticas previas a una restauracion que se conservan en el volumen. */
    private static final int COPIAS_PREVIAS_A_CONSERVAR = 5;
    private static final String PREFIJO_COPIA_PREVIA = "antes_de_restaurar_";
    private static final DateTimeFormatter SELLO = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private final JdbcTemplate jdbc;
    private final Path directorioCopias;

    public RespaldoService(JdbcTemplate jdbc,
                           @Value("${app.respaldos.directorio:respaldos}") String directorioCopias) {
        this.jdbc = jdbc;
        this.directorioCopias = Path.of(directorioCopias).toAbsolutePath();
    }

    /** Genera una copia consistente de la base de datos y devuelve sus bytes. */
    public byte[] generarCopia() {
        Path carpeta = null;
        Path destino = null;
        try {
            carpeta = Files.createTempDirectory("comerciocontrol-respaldo");
            destino = carpeta.resolve("comerciocontrol.db");
            // La ruta la genera la API; nunca viene del usuario.
            jdbc.execute("VACUUM INTO " + literalSql(destino));
            return Files.readAllBytes(destino);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar la copia de seguridad", e);
        } finally {
            borrar(destino);
            borrar(carpeta);
        }
    }

    /**
     * Reemplaza todos los datos por los de una copia generada con {@link #generarCopia()}.
     *
     * <p>Antes de tocar la base valida el archivo (formato SQLite, integridad y mismo
     * esquema que esta version) y guarda una copia del estado actual en
     * {@code app.respaldos.directorio}, para poder deshacer la restauracion.</p>
     *
     * @return nombre de la copia previa y verificacion de la base ya restaurada
     */
    public Map<String, Object> restaurar(byte[] contenido) {
        validarCabecera(contenido);
        Path carpeta = null;
        Path candidata = null;
        try {
            carpeta = Files.createTempDirectory("comerciocontrol-restauracion");
            candidata = carpeta.resolve("copia.db");
            Files.write(candidata, contenido);
            // El comando del controlador SQLite no admite comillas escapadas; la ruta
            // temporal la genera la API, asi que se descarta cualquier comilla.
            String ruta = candidata.toAbsolutePath().toString();
            if (ruta.contains("'")) {
                throw new IllegalStateException("La ruta temporal de la restauracion no es valida: " + ruta);
            }
            validarCopia(candidata);

            String copiaPrevia = guardarCopiaPrevia();
            jdbc.execute("restore from '" + ruta + "'");

            Map<String, Object> resultado = new LinkedHashMap<>();
            resultado.put("restaurada", true);
            resultado.put("copiaPrevia", copiaPrevia);
            resultado.put("verificacion", verificar());
            return resultado;
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo procesar la copia de seguridad", e);
        } finally {
            borrar(candidata);
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

    private static void validarCabecera(byte[] contenido) {
        if (contenido == null || contenido.length < 100
                || !Arrays.equals(Arrays.copyOf(contenido, CABECERA_SQLITE.length), CABECERA_SQLITE)) {
            throw new IllegalArgumentException("El archivo no es una copia de seguridad valida: "
                    + "use el .db que entrega GET /api/respaldos/descarga");
        }
    }

    /** Abre la copia en solo lectura y comprueba integridad y compatibilidad del esquema. */
    private void validarCopia(Path archivo) {
        SQLiteConfig config = new SQLiteConfig();
        config.setReadOnly(true);
        try (Connection con = config.createConnection("jdbc:sqlite:" + archivo.toAbsolutePath());
             Statement st = con.createStatement()) {
            String integridad;
            try (ResultSet rs = st.executeQuery("PRAGMA integrity_check")) {
                integridad = rs.next() ? rs.getString(1) : "sin respuesta";
            }
            if (!"ok".equals(integridad)) {
                throw new IllegalArgumentException("La copia esta danada: PRAGMA integrity_check devolvio '"
                        + integridad + "'");
            }
            for (String tabla : TABLAS) {
                Set<String> enCopia = new LinkedHashSet<>();
                try (ResultSet rs = st.executeQuery("PRAGMA table_info(" + tabla + ")")) {
                    while (rs.next()) {
                        enCopia.add(rs.getString("name"));
                    }
                }
                if (enCopia.isEmpty()) {
                    throw new IllegalArgumentException("La copia no contiene la tabla '" + tabla
                            + "': no es una copia de ComercioControl");
                }
                Set<String> actuales = new LinkedHashSet<>(jdbc.query("PRAGMA table_info(" + tabla + ")",
                        (rs, fila) -> rs.getString("name")));
                if (!enCopia.equals(actuales)) {
                    throw new IllegalArgumentException("La tabla '" + tabla + "' de la copia tiene columnas "
                            + enCopia + " y esta version espera " + actuales
                            + ". Use una copia de la misma version o migre primero el esquema");
                }
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException("El archivo no se pudo leer como base SQLite: " + e.getMessage());
        }
    }

    /** Guarda el estado actual antes de restaurar y conserva solo las ultimas copias previas. */
    private String guardarCopiaPrevia() throws IOException {
        Files.createDirectories(directorioCopias);
        String nombre = PREFIJO_COPIA_PREVIA + LocalDateTime.now().format(SELLO) + ".db";
        jdbc.execute("VACUUM INTO " + literalSql(directorioCopias.resolve(nombre)));
        try (Stream<Path> archivos = Files.list(directorioCopias)) {
            List<Path> copias = archivos
                    .filter(p -> p.getFileName().toString().startsWith(PREFIJO_COPIA_PREVIA))
                    .sorted(Comparator.reverseOrder())
                    .toList();
            for (Path vieja : copias.subList(Math.min(COPIAS_PREVIAS_A_CONSERVAR, copias.size()),
                    copias.size())) {
                Files.deleteIfExists(vieja);
            }
        }
        return nombre;
    }

    private static String literalSql(Path ruta) {
        return "'" + ruta.toAbsolutePath().toString().replace("'", "''") + "'";
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
