package com.comerciocontrol.api.service;

import com.comerciocontrol.api.dto.ProductoVendido;
import com.comerciocontrol.api.dto.ResumenInventario;
import com.comerciocontrol.api.dto.ResumenVentas;
import com.comerciocontrol.api.exception.ReglaNegocioException;
import com.comerciocontrol.api.model.Producto;
import com.comerciocontrol.api.repository.ProductoRepository;
import com.comerciocontrol.api.repository.VentaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/** Consultas agregadas que alimentan el dashboard y el modulo de reportes. */
@Service
public class ReporteService {

    private final VentaRepository ventas;
    private final ProductoRepository productos;

    public ReporteService(VentaRepository ventas, ProductoRepository productos) {
        this.ventas = ventas;
        this.productos = productos;
    }

    public ResumenVentas ventas(String desde, String hasta) {
        Rango rango = Rango.de(desde, hasta);
        return ventas.resumen(rango.desde(), rango.hasta());
    }

    public List<ProductoVendido> masVendidos(String desde, String hasta, int limite) {
        Rango rango = Rango.de(desde, hasta);
        return ventas.masVendidos(rango.desde(), rango.hasta(), Math.min(Math.max(limite, 1), 100));
    }

    public ResumenInventario inventario() {
        return productos.resumenInventario();
    }

    public List<Producto> stockCritico() {
        return productos.listarStockCritico();
    }

    /** Rango de fechas validado. Por defecto abarca el dia de hoy. */
    private record Rango(String desde, String hasta) {

        static Rango de(String desde, String hasta) {
            String hoy = LocalDate.now().toString();
            String inicio = (desde == null || desde.isBlank()) ? hoy : desde.trim();
            String fin = (hasta == null || hasta.isBlank()) ? hoy : hasta.trim();
            try {
                if (LocalDate.parse(inicio).isAfter(LocalDate.parse(fin))) {
                    throw new ReglaNegocioException(
                            "La fecha inicial (" + inicio + ") no puede ser posterior a la final (" + fin + ")");
                }
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(
                        "Las fechas deben tener el formato yyyy-MM-dd. Valor recibido: " + e.getParsedString());
            }
            return new Rango(inicio, fin);
        }
    }
}
