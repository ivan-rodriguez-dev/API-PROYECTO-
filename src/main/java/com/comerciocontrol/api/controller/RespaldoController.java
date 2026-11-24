package com.comerciocontrol.api.controller;

import com.comerciocontrol.api.exception.RespuestaError;
import com.comerciocontrol.api.security.RolesPermitidos;
import com.comerciocontrol.api.security.UsuarioActual;
import com.comerciocontrol.api.service.AuditoriaService;
import com.comerciocontrol.api.service.RespaldoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.dao.DataAccessException;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Respaldo, restauracion y verificacion de la base de datos. Solo para el rol administrador.
 *
 * <p>Permite sacar una copia fuera de la plataforma de publicacion (regla 3-2-1),
 * devolverla al sistema cuando haga falta (o cargarla en una plataforma nueva durante
 * una migracion) y comprobar la integridad despues de cada uno de esos pasos.</p>
 */
@RestController
@RequestMapping("/api/respaldos")
@RolesPermitidos({"administrador"})
@Tag(name = "10 - Respaldos",
        description = "Copia de seguridad y verificacion de integridad. Solo para el rol administrador")
public class RespaldoController {

    private static final DateTimeFormatter SELLO = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final String CONFIRMACION = "RESTAURAR";

    private final RespaldoService respaldoService;
    private final AuditoriaService auditoriaService;
    private final UsuarioActual usuarioActual;

    public RespaldoController(RespaldoService respaldoService, AuditoriaService auditoriaService,
                              UsuarioActual usuarioActual) {
        this.respaldoService = respaldoService;
        this.auditoriaService = auditoriaService;
        this.usuarioActual = usuarioActual;
    }

    @GetMapping("/descarga")
    @Operation(summary = "Descargar una copia de seguridad de la base de datos",
            description = "Genera una copia consistente con VACUUM INTO, sin detener la API, "
                    + "y la entrega como archivo .db. Queda registrada en la auditoria.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Archivo SQLite con la copia"),
            @ApiResponse(responseCode = "403", description = "Solo el administrador puede descargarla",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    public ResponseEntity<byte[]> descargar() {
        byte[] copia = respaldoService.generarCopia();
        String nombre = "comerciocontrol_" + LocalDateTime.now().format(SELLO) + ".db";
        auditoriaService.registrar(usuarioActual.id(), "DESCARGAR_RESPALDO", "sistema",
                "Copia de seguridad " + nombre + " (" + copia.length + " bytes)");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nombre).build().toString())
                .cacheControl(CacheControl.noStore())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(copia);
    }

    @PostMapping(value = "/restauracion", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Restaurar la base de datos desde una copia de seguridad",
            description = "Reemplaza TODOS los datos actuales por los de una copia (.db) descargada con "
                    + "/api/respaldos/descarga. Antes valida el archivo (formato, integridad y esquema) "
                    + "y guarda una copia automatica del estado actual. Escriba RESTAURAR en "
                    + "confirmacion. Queda registrada en la auditoria.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Copia restaurada y verificada"),
            @ApiResponse(responseCode = "400",
                    description = "Archivo invalido, danado, de otra version del esquema o sin confirmacion",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "403", description = "Solo el administrador puede restaurar",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    public Map<String, Object> restaurar(
            @Parameter(description = "Archivo .db descargado con /api/respaldos/descarga")
            @RequestParam("archivo") MultipartFile archivo,
            @Parameter(description = "Escriba RESTAURAR para aceptar que se reemplacen los datos actuales")
            @RequestParam("confirmacion") String confirmacion) throws IOException {
        if (!CONFIRMACION.equals(confirmacion)) {
            throw new IllegalArgumentException("Para restaurar escriba " + CONFIRMACION
                    + " en el campo confirmacion: la operacion reemplaza todos los datos actuales");
        }
        Map<String, Object> resultado = respaldoService.restaurar(archivo.getBytes());
        try {
            auditoriaService.registrar(usuarioActual.id(), "RESTAURAR_RESPALDO", "sistema",
                    "Restaurada la copia " + archivo.getOriginalFilename() + " (" + archivo.getSize()
                            + " bytes); estado anterior guardado en " + resultado.get("copiaPrevia"));
            resultado.put("auditoria", "registrada");
        } catch (DataAccessException e) {
            // Una copia de otra instalacion puede no tener al usuario de esta sesion.
            resultado.put("auditoria", "no registrada: el usuario de la sesion no existe en la copia");
        }
        return resultado;
    }

    @GetMapping("/verificacion")
    @Operation(summary = "Verificar la integridad de la base de datos",
            description = "Ejecuta PRAGMA integrity_check y cuenta los registros de cada tabla. "
                    + "Se usa despues de un mantenimiento, una restauracion o una migracion.")
    public Map<String, Object> verificar() {
        return respaldoService.verificar();
    }
}
