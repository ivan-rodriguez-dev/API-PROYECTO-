package com.comerciocontrol.api.controller;

import com.comerciocontrol.api.dto.ProveedorRequest;
import com.comerciocontrol.api.exception.RespuestaError;
import com.comerciocontrol.api.model.Proveedor;
import com.comerciocontrol.api.security.RolesPermitidos;
import com.comerciocontrol.api.security.UsuarioActual;
import com.comerciocontrol.api.service.ProveedorService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/** Servicios web del modulo de proveedores. */
@RestController
@RequestMapping("/api/proveedores")
@RolesPermitidos({"administrador", "bodeguero"})
@Tag(name = "04 - Proveedores", description = "Gestion de los proveedores que surten el inventario")
public class ProveedorController {

    private final ProveedorService servicio;
    private final UsuarioActual usuarioActual;

    public ProveedorController(ProveedorService servicio, UsuarioActual usuarioActual) {
        this.servicio = servicio;
        this.usuarioActual = usuarioActual;
    }

    @GetMapping
    @Operation(summary = "Listar proveedores")
    public List<Proveedor> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un proveedor por su id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proveedor encontrado"),
            @ApiResponse(responseCode = "404", description = "El proveedor no existe",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    public Proveedor obtener(@PathVariable int id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @Operation(summary = "Registrar un proveedor", description = "El NIT debe ser unico.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Proveedor creado"),
            @ApiResponse(responseCode = "409", description = "Ya existe un proveedor con ese NIT",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    public ResponseEntity<Proveedor> crear(@Valid @RequestBody ProveedorRequest peticion) {
        Proveedor creado = servicio.crear(peticion, usuarioActual.id());
        return ResponseEntity.created(URI.create("/api/proveedores/" + creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un proveedor")
    public Proveedor actualizar(@PathVariable int id, @Valid @RequestBody ProveedorRequest peticion) {
        return servicio.actualizar(id, peticion, usuarioActual.id());
    }

    @DeleteMapping("/{id}")
    @RolesPermitidos({"administrador"})
    @Operation(summary = "Dar de baja un proveedor")
    @ApiResponse(responseCode = "204", description = "Proveedor dado de baja")
    public ResponseEntity<Void> desactivar(@PathVariable int id) {
        servicio.desactivar(id, usuarioActual.id());
        return ResponseEntity.noContent().build();
    }
}
