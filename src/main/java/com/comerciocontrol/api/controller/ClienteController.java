package com.comerciocontrol.api.controller;

import com.comerciocontrol.api.dto.ClienteRequest;
import com.comerciocontrol.api.exception.RespuestaError;
import com.comerciocontrol.api.model.Cliente;
import com.comerciocontrol.api.security.RolesPermitidos;
import com.comerciocontrol.api.security.UsuarioActual;
import com.comerciocontrol.api.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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

/** Servicios web del modulo de clientes. */
@RestController
@RequestMapping("/api/clientes")
@RolesPermitidos({"administrador", "vendedor"})
@Tag(name = "03 - Clientes", description = "Gestion de los clientes del comercio")
public class ClienteController {

    private final ClienteService servicio;
    private final UsuarioActual usuarioActual;

    public ClienteController(ClienteService servicio, UsuarioActual usuarioActual) {
        this.servicio = servicio;
        this.usuarioActual = usuarioActual;
    }

    @GetMapping
    @Operation(summary = "Listar clientes",
            description = "Permite buscar por nombre o por cedula con el parametro 'q'.")
    public List<Cliente> listar(
            @Parameter(description = "Texto a buscar en el nombre o la cedula", example = "ana")
            @RequestParam(required = false) String q) {
        return servicio.listar(q);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un cliente por su id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "El cliente no existe",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    public Cliente obtener(@PathVariable int id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @Operation(summary = "Registrar un cliente",
            description = "La cedula, si se envia, debe ser unica en el sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente creado"),
            @ApiResponse(responseCode = "409", description = "Ya existe un cliente con esa cedula",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    public ResponseEntity<Cliente> crear(@Valid @RequestBody ClienteRequest peticion) {
        Cliente creado = servicio.crear(peticion, usuarioActual.id());
        return ResponseEntity.created(URI.create("/api/clientes/" + creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un cliente")
    public Cliente actualizar(@PathVariable int id, @Valid @RequestBody ClienteRequest peticion) {
        return servicio.actualizar(id, peticion, usuarioActual.id());
    }

    @DeleteMapping("/{id}")
    @RolesPermitidos({"administrador"})
    @Operation(summary = "Dar de baja un cliente",
            description = "Baja logica para conservar el historial de compras asociado.")
    @ApiResponse(responseCode = "204", description = "Cliente dado de baja")
    public ResponseEntity<Void> desactivar(@PathVariable int id) {
        servicio.desactivar(id, usuarioActual.id());
        return ResponseEntity.noContent().build();
    }
}
