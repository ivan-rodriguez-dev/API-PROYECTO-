package com.comerciocontrol.api.controller;

import com.comerciocontrol.api.exception.RespuestaError;
import com.comerciocontrol.api.security.RolesPermitidos;
import com.comerciocontrol.api.security.UsuarioActual;
import com.comerciocontrol.api.service.AuditoriaService;
import com.comerciocontrol.api.service.RespaldoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Respaldo y verificacion de la base de datos. Solo para el rol administrador.
 *
 * <p>Permite sacar una copia fuera de la plataforma de publicacion (regla 3-2-1)
 * y comprobar la integridad despues de un mantenimiento, una restauracion o
 * una migracion.</p>
 */
@RestController
@RequestMapping("/api/respaldos")
@RolesPermitidos({"administrador"})
@Tag(name = "10 - Respaldos",
        description = "Copia de seguridad y verificacion de integridad. Solo para el rol administrador")
public class RespaldoController {

    private static final DateTimeFormatter SELLO = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

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

    @GetMapping("/verificacion")
    @Operation(summary = "Verificar la integridad de la base de datos",
            description = "Ejecuta PRAGMA integrity_check y cuenta los registros de cada tabla. "
                    + "Se usa despues de un mantenimiento, una restauracion o una migracion.")
    public Map<String, Object> verificar() {
        return respaldoService.verificar();
    }
}
