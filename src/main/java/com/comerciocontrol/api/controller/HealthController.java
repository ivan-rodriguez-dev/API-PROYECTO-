package com.comerciocontrol.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Verificacion de estado de la API. Es el unico recurso publico junto con el login. */
@RestController
@RequestMapping("/api/health")
@Tag(name = "00 - Estado", description = "Comprobacion de disponibilidad de la API")
public class HealthController {

    private final JdbcTemplate jdbc;
    private final String nombreNegocio;

    public HealthController(JdbcTemplate jdbc, @Value("${app.negocio.nombre}") String nombreNegocio) {
        this.jdbc = jdbc;
        this.nombreNegocio = nombreNegocio;
    }

    @GetMapping
    @SecurityRequirements
    @Operation(summary = "Estado de la API y de la base de datos",
            description = "Devuelve 200 si la API responde y la conexion con SQLite esta activa. "
                    + "Es la primera peticion que se ejecuta en la coleccion de Postman.")
    public Map<String, Object> estado() {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("servicio", "ComercioControl API");
        respuesta.put("version", "1.0.0");
        respuesta.put("negocio", nombreNegocio);
        respuesta.put("fecha", LocalDateTime.now());
        try {
            Integer productos = jdbc.queryForObject("SELECT COUNT(*) FROM productos", Integer.class);
            respuesta.put("estado", "OPERATIVA");
            respuesta.put("baseDatos", "SQLite conectada");
            respuesta.put("productosRegistrados", productos);
        } catch (Exception e) {
            respuesta.put("estado", "DEGRADADA");
            respuesta.put("baseDatos", "Sin conexion: " + e.getMessage());
        }
        return respuesta;
    }
}
