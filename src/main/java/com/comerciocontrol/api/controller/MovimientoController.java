package com.comerciocontrol.api.controller;

import com.comerciocontrol.api.dto.MovimientoRequest;
import com.comerciocontrol.api.exception.RespuestaError;
import com.comerciocontrol.api.model.Movimiento;
import com.comerciocontrol.api.security.RolesPermitidos;
import com.comerciocontrol.api.security.UsuarioActual;
import com.comerciocontrol.api.service.MovimientoService;
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

/** Servicios web de los movimientos de stock (kardex). */
@RestController
@RequestMapping("/api/movimientos")
@RolesPermitidos({"administrador", "bodeguero"})
@Tag(name = "06 - Movimientos de stock",
        description = "Entradas, salidas y ajustes de inventario con trazabilidad")
public class MovimientoController {

    private final MovimientoService servicio;
    private final UsuarioActual usuarioActual;

    public MovimientoController(MovimientoService servicio, UsuarioActual usuarioActual) {
        this.servicio = servicio;
        this.usuarioActual = usuarioActual;
    }

    @GetMapping
    @Operation(summary = "Listar movimientos",
            description = "Kardex ordenado del mas reciente al mas antiguo, con filtros opcionales.")
    public List<Movimiento> listar(
            @Parameter(description = "Filtrar por producto", example = "2")
            @RequestParam(required = false) Integer productoId,
            @Parameter(description = "Filtrar por tipo", example = "entrada",
                    schema = @Schema(allowableValues = {"entrada", "salida", "ajuste"}))
            @RequestParam(required = false) String tipo) {
        return servicio.listar(productoId, tipo);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un movimiento por su id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movimiento encontrado"),
            @ApiResponse(responseCode = "404", description = "El movimiento no existe",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    public Movimiento obtener(@PathVariable int id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @Operation(summary = "Registrar un movimiento de stock",
            description = """
                    Actualiza el inventario y deja constancia en el kardex:

                    - **entrada**: suma unidades (compra a proveedor o devolucion).
                    - **salida**: resta unidades; se rechaza con 409 si no hay stock suficiente.
                    - **ajuste**: fija el stock al valor contado en un inventario fisico.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Movimiento registrado"),
            @ApiResponse(responseCode = "404", description = "El producto no existe",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "409", description = "Stock insuficiente para la salida",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    public ResponseEntity<Movimiento> registrar(@Valid @RequestBody MovimientoRequest peticion) {
        Movimiento movimiento = servicio.registrar(peticion, usuarioActual.id());
        return ResponseEntity.created(URI.create("/api/movimientos/" + movimiento.id())).body(movimiento);
    }
}
