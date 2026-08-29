package com.comerciocontrol.api.controller;

import com.comerciocontrol.api.dto.VentaRequest;
import com.comerciocontrol.api.exception.RespuestaError;
import com.comerciocontrol.api.model.Venta;
import com.comerciocontrol.api.security.RolesPermitidos;
import com.comerciocontrol.api.security.UsuarioActual;
import com.comerciocontrol.api.service.VentaService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/** Servicios web del punto de venta (POS). */
@RestController
@RequestMapping("/api/ventas")
@RolesPermitidos({"administrador", "vendedor"})
@Tag(name = "05 - Ventas (POS)",
        description = "Registro y consulta de ventas. Es el proceso de negocio central del sistema")
public class VentaController {

    private final VentaService servicio;
    private final UsuarioActual usuarioActual;

    public VentaController(VentaService servicio, UsuarioActual usuarioActual) {
        this.servicio = servicio;
        this.usuarioActual = usuarioActual;
    }

    @GetMapping
    @Operation(summary = "Listar ventas por rango de fechas",
            description = "Si no se envian fechas devuelve las ventas del dia de hoy. "
                    + "Formato de las fechas: yyyy-MM-dd.")
    public List<Venta> listar(
            @Parameter(description = "Fecha inicial", example = "2026-08-01")
            @RequestParam(required = false) String desde,
            @Parameter(description = "Fecha final", example = "2026-08-31")
            @RequestParam(required = false) String hasta) {
        return servicio.listar(desde, hasta);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar una venta con su detalle",
            description = "Devuelve la cabecera y las lineas del carrito con su subtotal.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venta encontrada"),
            @ApiResponse(responseCode = "404", description = "La venta no existe",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    public Venta obtener(@PathVariable int id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @Operation(summary = "Registrar una venta",
            description = """
                    Operacion transaccional que aplica las reglas de negocio del proyecto:

                    1. Exige que exista una **caja abierta**.
                    2. Verifica que cada producto exista y este activo.
                    3. Descuenta el stock garantizando que **nunca quede negativo**.
                    4. Calcula subtotal, descuento, IVA del 19 % y total **en el servidor**.
                    5. Registra el detalle, el movimiento de salida y la traza de auditoria.

                    Si algun paso falla se revierte la transaccion completa.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Venta registrada"),
            @ApiResponse(responseCode = "400", description = "El carrito viene vacio o con datos invalidos",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "404", description = "Algun producto o el cliente no existe",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "409",
                    description = "No hay caja abierta, no hay stock suficiente o el descuento supera el subtotal",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    public ResponseEntity<Venta> registrar(@Valid @RequestBody VentaRequest peticion) {
        Venta venta = servicio.registrar(peticion, usuarioActual.id());
        return ResponseEntity.created(URI.create("/api/ventas/" + venta.id())).body(venta);
    }
}
