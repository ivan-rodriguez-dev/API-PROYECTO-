package com.comerciocontrol.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la API REST de ComercioControl.
 *
 * <p>Expone como servicios web los procesos de negocio del proyecto formativo:
 * autenticacion, inventario, clientes, proveedores, ventas (POS), movimientos
 * de stock, caja diaria, reportes y auditoria.</p>
 *
 * <p>Evidencia GA7-220501096-AA5-EV04 - Ivan Manuel Rodriguez David - Ficha 3235886</p>
 */
@SpringBootApplication
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}
