package com.comerciocontrol.api.controller;

import com.comerciocontrol.api.dto.LoginRequest;
import com.comerciocontrol.api.dto.LoginResponse;
import com.comerciocontrol.api.exception.RespuestaError;
import com.comerciocontrol.api.model.Usuario;
import com.comerciocontrol.api.security.UsuarioActual;
import com.comerciocontrol.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Servicios de autenticacion: inicio de sesion y consulta del perfil. */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "01 - Autenticacion", description = "Inicio de sesion y emision de tokens JWT")
public class AuthController {

    private final AuthService servicio;
    private final UsuarioActual usuarioActual;

    public AuthController(AuthService servicio, UsuarioActual usuarioActual) {
        this.servicio = servicio;
        this.usuarioActual = usuarioActual;
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Iniciar sesion",
            description = "Valida las credenciales contra la tabla usuarios y devuelve un token JWT "
                    + "que debe enviarse en la cabecera Authorization del resto de peticiones.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credenciales correctas, token emitido"),
            @ApiResponse(responseCode = "400", description = "Faltan campos obligatorios",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "401", description = "Usuario o contrasena incorrectos",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest peticion) {
        return ResponseEntity.ok(servicio.login(peticion));
    }

    @GetMapping("/perfil")
    @Operation(summary = "Perfil del usuario autenticado",
            description = "Devuelve los datos del usuario dueno del token enviado. "
                    + "Sirve para comprobar que el token es valido.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil del usuario"),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido o expirado",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    public ResponseEntity<Usuario> perfil() {
        return ResponseEntity.ok(servicio.perfil(usuarioActual.id()));
    }
}
