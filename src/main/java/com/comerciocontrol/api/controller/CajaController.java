package com.comerciocontrol.api.controller;

import com.comerciocontrol.api.dto.AperturaCajaRequest;
import com.comerciocontrol.api.dto.CierreCajaRequest;
import com.comerciocontrol.api.exception.RespuestaError;
import com.comerciocontrol.api.model.Caja;
import com.comerciocontrol.api.security.RolesPermitidos;
import com.comerciocontrol.api.security.UsuarioActual;
import com.comerciocontrol.api.service.CajaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Servicios web de la caja diaria. */
@RestController
@RequestMapping("/api/caja")
@RolesPermitidos({"administrador", "vendedor"})
@Tag(name = "07 - Caja", description = "Apertura y cierre de la jornada de caja")
public class CajaController {

    private final CajaService servicio;
    private final UsuarioActual usuarioActual;

    public CajaController(CajaService servicio, UsuarioActual usuarioActual) {
        this.servicio = servicio;
        this.usuarioActual = usuarioActual;
    }

    @GetMapping("/estado")
    @Operation(summary = "Consultar el estado de la caja",
            description = "Indica si hay una caja abierta. El punto de venta lo consulta antes de "
                    + "habilitar el registro de ventas.")
    public Map<String, Object> estado() {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        servicio.cajaAbierta().ifPresentOrElse(caja -> {
            respuesta.put("hayCajaAbierta", true);
            respuesta.put("caja", caja);
        }, () -> {
            respuesta.put("hayCajaAbierta", false);
            respuesta.put("mensaje", "No hay una caja abierta. No se pueden registrar ventas");
        });
        return respuesta;
    }

    @GetMapping
    @Operation(summary = "Historial de jornadas de caja")
    public List<Caja> listar() {
        return servicio.listar();
    }

    @PostMapping("/apertura")
    @Operation(summary = "Abrir la caja",
            description = "Registra la base en efectivo con la que inicia la jornada. "
                    + "Solo puede haber una caja abierta a la vez.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Caja abierta"),
            @ApiResponse(responseCode = "409", description = "Ya hay una caja abierta",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    public ResponseEntity<Caja> abrir(@Valid @RequestBody AperturaCajaRequest peticion) {
        return ResponseEntity.status(201).body(servicio.abrir(peticion, usuarioActual.id()));
    }

    @PostMapping("/cierre")
    @Operation(summary = "Cerrar la caja",
            description = "Calcula la diferencia como "
                    + "`efectivo contado - (base de apertura + ventas del dia)`. "
                    + "Una diferencia negativa significa faltante y una positiva sobrante.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Caja cerrada con el cuadre calculado"),
            @ApiResponse(responseCode = "409", description = "No hay ninguna caja abierta",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    public Caja cerrar(@Valid @RequestBody CierreCajaRequest peticion) {
        return servicio.cerrar(peticion, usuarioActual.id());
    }
}
