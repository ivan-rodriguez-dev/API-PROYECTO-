package com.comerciocontrol.api.controller;

import com.comerciocontrol.api.dto.UsuarioRequest;
import com.comerciocontrol.api.exception.RespuestaError;
import com.comerciocontrol.api.model.Auditoria;
import com.comerciocontrol.api.model.Usuario;
import com.comerciocontrol.api.security.RolesPermitidos;
import com.comerciocontrol.api.security.UsuarioActual;
import com.comerciocontrol.api.service.AuditoriaService;
import com.comerciocontrol.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * Servicios web reservados al administrador: gestion de usuarios y consulta de auditoria.
 */
@RestController
@RequestMapping("/api")
@RolesPermitidos({"administrador"})
@Tag(name = "09 - Administracion",
        description = "Usuarios, roles y traza de auditoria. Solo para el rol administrador")
public class AdministracionController {

    private final AuthService authService;
    private final AuditoriaService auditoriaService;
    private final UsuarioActual usuarioActual;

    public AdministracionController(AuthService authService, AuditoriaService auditoriaService,
                                    UsuarioActual usuarioActual) {
        this.authService = authService;
        this.auditoriaService = auditoriaService;
        this.usuarioActual = usuarioActual;
    }

    @GetMapping("/usuarios")
    @Operation(summary = "Listar usuarios del sistema",
            description = "El hash de la contrasena nunca se incluye en la respuesta.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de usuarios"),
            @ApiResponse(responseCode = "403", description = "Solo el administrador puede consultarlo",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    public List<Usuario> usuarios() {
        return authService.listar();
    }

    @PostMapping("/usuarios")
    @Operation(summary = "Crear un usuario",
            description = "La contrasena se almacena cifrada con SHA-256; nunca en texto plano.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado"),
            @ApiResponse(responseCode = "409", description = "El nombre de cuenta ya existe",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    public ResponseEntity<Usuario> crearUsuario(@Valid @RequestBody UsuarioRequest peticion) {
        Usuario creado = authService.crear(peticion, usuarioActual.id());
        return ResponseEntity.created(URI.create("/api/usuarios/" + creado.id())).body(creado);
    }

    @PutMapping("/usuarios/{id}/estado")
    @Operation(summary = "Activar o desactivar un usuario",
            description = "Un usuario inactivo no puede iniciar sesion.")
    public Usuario cambiarEstado(@PathVariable int id,
                                 @Parameter(description = "true para activar, false para desactivar")
                                 @RequestParam boolean activo) {
        return authService.cambiarEstado(id, activo, usuarioActual.id());
    }

    @GetMapping("/auditoria")
    @Operation(summary = "Consultar la traza de auditoria",
            description = "Registro de quien ejecuto cada operacion, sobre que tabla y cuando. "
                    + "Se devuelve del evento mas reciente al mas antiguo.")
    public List<Auditoria> auditoria(
            @Parameter(description = "Filtrar por tabla afectada", example = "ventas")
            @RequestParam(required = false) String tabla,
            @Parameter(description = "Filtrar por usuario responsable", example = "1")
            @RequestParam(required = false) Integer usuarioId,
            @Parameter(description = "Maximo de registros a devolver", example = "50")
            @RequestParam(defaultValue = "100") int limite) {
        return auditoriaService.listar(tabla, usuarioId, limite);
    }
}
