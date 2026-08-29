package com.comerciocontrol.api.controller;

import com.comerciocontrol.api.dto.ProductoVendido;
import com.comerciocontrol.api.dto.ResumenInventario;
import com.comerciocontrol.api.dto.ResumenVentas;
import com.comerciocontrol.api.model.Producto;
import com.comerciocontrol.api.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Servicios web de reportes y dashboard. Disponibles para todos los roles autenticados. */
@RestController
@RequestMapping("/api/reportes")
@Tag(name = "08 - Reportes", description = "Consultas agregadas que alimentan el dashboard")
public class ReporteController {

    private final ReporteService servicio;

    public ReporteController(ReporteService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/ventas")
    @Operation(summary = "Resumen de ventas por rango de fechas",
            description = "Cantidad de ventas, subtotal, descuentos, IVA, total y ticket promedio. "
                    + "Si no se envian fechas usa el dia de hoy.")
    public ResumenVentas ventas(
            @Parameter(description = "Fecha inicial (yyyy-MM-dd)", example = "2026-08-01")
            @RequestParam(required = false) String desde,
            @Parameter(description = "Fecha final (yyyy-MM-dd)", example = "2026-08-31")
            @RequestParam(required = false) String hasta) {
        return servicio.ventas(desde, hasta);
    }

    @GetMapping("/mas-vendidos")
    @Operation(summary = "Ranking de productos mas vendidos",
            description = "Ordenado por unidades vendidas en el rango indicado.")
    public List<ProductoVendido> masVendidos(
            @Parameter(description = "Fecha inicial (yyyy-MM-dd)", example = "2026-08-01")
            @RequestParam(required = false) String desde,
            @Parameter(description = "Fecha final (yyyy-MM-dd)", example = "2026-08-31")
            @RequestParam(required = false) String hasta,
            @Parameter(description = "Cantidad maxima de filas", example = "5")
            @RequestParam(defaultValue = "10") int limite) {
        return servicio.masVendidos(desde, hasta, limite);
    }

    @GetMapping("/inventario")
    @Operation(summary = "Resumen del inventario",
            description = "Total de productos, unidades, valorizacion a costo y a venta, "
                    + "utilidad potencial y cantidad de productos en estado critico.")
    public ResumenInventario inventario() {
        return servicio.inventario();
    }

    @GetMapping("/stock-critico")
    @Operation(summary = "Listado de productos que requieren reposicion")
    public List<Producto> stockCritico() {
        return servicio.stockCritico();
    }
}
