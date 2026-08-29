package com.comerciocontrol.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integracion de los servicios web de ComercioControl.
 *
 * <p>Cubren el camino feliz y los errores controlados de cada regla de negocio:
 * autenticacion, permisos por rol, validaciones de entrada, venta sin caja abierta
 * y venta sin stock suficiente.</p>
 *
 * <p>Se ejecutan contra una base de datos SQLite propia que se borra antes de empezar,
 * de modo que el resultado sea siempre el mismo.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:sqlite:target/test-comerciocontrol.db"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ComercioControlApiTest {

    static {
        // Base de datos limpia en cada ejecucion de la suite.
        new File("target/test-comerciocontrol.db").delete();
    }

    private static final ObjectMapper JSON = new ObjectMapper();

    private static String tokenAdmin;
    private static String tokenVendedor;

    @Autowired
    private MockMvc mvc;

    // ---------------------------------------------------------------- estado

    @Test
    @Order(1)
    void laApiRespondeYLaBaseDeDatosEstaConectada() throws Exception {
        mvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("OPERATIVA"))
                .andExpect(jsonPath("$.baseDatos").value("SQLite conectada"));
    }

    // -------------------------------------------------------- autenticacion

    @Test
    @Order(2)
    void elLoginCorrectoDevuelveToken() throws Exception {
        MvcResult resultado = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"ivan.admin\",\"password\":\"Admin2026*\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.rol").value("administrador"))
                .andReturn();

        tokenAdmin = leer(resultado, "token");
        assertTrue(tokenAdmin != null && !tokenAdmin.isBlank(), "El login debe devolver un token");
    }

    @Test
    @Order(3)
    void elLoginConPasswordIncorrectaDevuelve401() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"ivan.admin\",\"password\":\"claveIncorrecta\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.estado").value(401));
    }

    @Test
    @Order(4)
    void elLoginSinCamposObligatoriosDevuelve400() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.usuario").exists());
    }

    @Test
    @Order(5)
    void unaPeticionSinTokenDevuelve401() throws Exception {
        mvc.perform(get("/api/productos"))
                .andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------ inventario

    @Test
    @Order(6)
    void seListanLosProductosConTokenValido() throws Exception {
        mvc.perform(get("/api/productos").header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").exists())
                .andExpect(jsonPath("$[0].estado").exists());
    }

    @Test
    @Order(7)
    void seDetectanLosProductosConStockCritico() throws Exception {
        mvc.perform(get("/api/productos/stock-critico")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("CRITICO"));
    }

    @Test
    @Order(8)
    void unProductoInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/api/productos/9999").header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404));
    }

    @Test
    @Order(9)
    void crearUnProductoConDatosInvalidosDevuelve400() throws Exception {
        mvc.perform(post("/api/productos")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"","nombre":"","precioCosto":-5,
                                 "precioVenta":100,"stockActual":-1,"stockMinimo":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.codigo").exists())
                .andExpect(jsonPath("$.detalles.nombre").exists());
    }

    @Test
    @Order(10)
    void crearUnProductoConPrecioDeVentaMenorAlCostoDevuelve409() throws Exception {
        mvc.perform(post("/api/productos")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"P-900","nombre":"Producto de prueba","categoria":"Pruebas",
                                 "precioCosto":5000,"precioVenta":3000,"stockActual":10,"stockMinimo":2}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Regla de negocio"));
    }

    @Test
    @Order(11)
    void seCreaUnProductoValido() throws Exception {
        mvc.perform(post("/api/productos")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"P-901","nombre":"Panela x 500 g","categoria":"Abarrotes",
                                 "precioCosto":2100,"precioVenta":3200,"stockActual":40,"stockMinimo":10}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("P-901"))
                .andExpect(jsonPath("$.estado").value("OK"))
                .andExpect(jsonPath("$.utilidadUnitaria").value(1100.0));
    }

    @Test
    @Order(12)
    void noSePuedeRepetirElCodigoDeProducto() throws Exception {
        mvc.perform(post("/api/productos")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"P-901","nombre":"Duplicado","categoria":"Abarrotes",
                                 "precioCosto":2100,"precioVenta":3200,"stockActual":5,"stockMinimo":1}
                                """))
                .andExpect(status().isConflict());
    }

    // ------------------------------------------------------ control por rol

    @Test
    @Order(13)
    void elVendedorNoPuedeCrearProductos() throws Exception {
        MvcResult login = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"laura.ventas\",\"password\":\"Venta2026*\"}"))
                .andExpect(status().isOk())
                .andReturn();
        tokenVendedor = leer(login, "token");

        mvc.perform(post("/api/productos")
                        .header("Authorization", "Bearer " + tokenVendedor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"P-902","nombre":"No permitido","precioCosto":100,
                                 "precioVenta":200,"stockActual":1,"stockMinimo":1}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Acceso denegado"));
    }

    @Test
    @Order(14)
    void elVendedorNoPuedeConsultarLaAuditoria() throws Exception {
        mvc.perform(get("/api/auditoria").header("Authorization", "Bearer " + tokenVendedor))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------- ventas y caja

    @Test
    @Order(15)
    void noSePuedeVenderSinCajaAbierta() throws Exception {
        mvc.perform(post("/api/ventas")
                        .header("Authorization", "Bearer " + tokenVendedor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":2,\"descuento\":0,\"items\":[{\"productoId\":1,\"cantidad\":2}]}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value(
                        org.hamcrest.Matchers.containsString("No hay una caja abierta")));
    }

    @Test
    @Order(16)
    void seAbreLaCaja() throws Exception {
        mvc.perform(post("/api/caja/apertura")
                        .header("Authorization", "Bearer " + tokenVendedor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"apertura\":100000,\"observacion\":\"Apertura de pruebas\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("abierta"))
                .andExpect(jsonPath("$.apertura").value(100000.0));
    }

    @Test
    @Order(17)
    void noSePuedeAbrirUnaSegundaCaja() throws Exception {
        mvc.perform(post("/api/caja/apertura")
                        .header("Authorization", "Bearer " + tokenVendedor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"apertura\":50000}"))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(18)
    void seRegistraUnaVentaYSeCalculanLosImportesEnElServidor() throws Exception {
        // 3 x Arroz (2500) + 2 x Leche (4900) = 7500 + 9800 = 17300 de subtotal
        // base = 17300 - 300 = 17000 ; IVA 19% = 3230 ; total = 20230
        mvc.perform(post("/api/ventas")
                        .header("Authorization", "Bearer " + tokenVendedor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":2,"descuento":300,
                                 "items":[{"productoId":1,"cantidad":3},{"productoId":3,"cantidad":2}]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subtotal").value(17300.0))
                .andExpect(jsonPath("$.descuento").value(300.0))
                .andExpect(jsonPath("$.iva").value(3230.0))
                .andExpect(jsonPath("$.total").value(20230.0))
                .andExpect(jsonPath("$.detalles.length()").value(2));
    }

    @Test
    @Order(19)
    void laVentaDescontoElStockDelProducto() throws Exception {
        // El arroz (id 1) partia de 120 unidades y se vendieron 3.
        mvc.perform(get("/api/productos/1").header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockActual").value(117));
    }

    @Test
    @Order(20)
    void laVentaDejoMovimientoDeSalidaEnElKardex() throws Exception {
        mvc.perform(get("/api/movimientos?productoId=1&tipo=salida")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cantidad").value(3))
                .andExpect(jsonPath("$[0].stockResultante").value(117));
    }

    @Test
    @Order(21)
    void noSePuedeVenderMasStockDelDisponible() throws Exception {
        // El aceite (id 2) solo tiene 4 unidades en existencia.
        mvc.perform(post("/api/ventas")
                        .header("Authorization", "Bearer " + tokenVendedor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descuento\":0,\"items\":[{\"productoId\":2,\"cantidad\":50}]}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value(
                        org.hamcrest.Matchers.containsString("Stock insuficiente")));
    }

    @Test
    @Order(22)
    void laVentaRechazadaNoModificoElStock() throws Exception {
        mvc.perform(get("/api/productos/2").header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockActual").value(4));
    }

    @Test
    @Order(23)
    void unaVentaSinProductosDevuelve400() throws Exception {
        mvc.perform(post("/api/ventas")
                        .header("Authorization", "Bearer " + tokenVendedor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descuento\":0,\"items\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(24)
    void elDescuentoNoPuedeSuperarElSubtotal() throws Exception {
        mvc.perform(post("/api/ventas")
                        .header("Authorization", "Bearer " + tokenVendedor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descuento\":999999,\"items\":[{\"productoId\":1,\"cantidad\":1}]}"))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(25)
    void elCierreDeCajaCalculaLaDiferencia() throws Exception {
        // Apertura 100000 + ventas 20230 = 120230 esperados. Se cuentan 120000: faltan 230.
        mvc.perform(post("/api/caja/cierre")
                        .header("Authorization", "Bearer " + tokenVendedor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"efectivoContado\":120000,\"observacion\":\"Cierre de pruebas\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("cerrada"))
                .andExpect(jsonPath("$.ventasDia").value(20230.0))
                .andExpect(jsonPath("$.diferencia").value(-230.0));
    }

    // ------------------------------------------------------------- reportes

    @Test
    @Order(26)
    void elReporteDeVentasConsolidaElPeriodo() throws Exception {
        mvc.perform(get("/api/reportes/ventas").header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadVentas").value(1))
                .andExpect(jsonPath("$.total").value(20230.0));
    }

    @Test
    @Order(27)
    void elReporteDeInventarioValorizaElStock() throws Exception {
        mvc.perform(get("/api/reportes/inventario").header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProductos").value(8))
                .andExpect(jsonPath("$.productosCriticos").value(
                        org.hamcrest.Matchers.greaterThanOrEqualTo(2)));
    }

    @Test
    @Order(28)
    void elReporteDeMasVendidosOrdenaPorUnidades() throws Exception {
        mvc.perform(get("/api/reportes/mas-vendidos?limite=5")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].unidadesVendidas").value(3));
    }

    @Test
    @Order(29)
    void elRangoDeFechasInvalidoDevuelve409() throws Exception {
        mvc.perform(get("/api/reportes/ventas?desde=2026-12-31&hasta=2026-01-01")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isConflict());
    }

    // ------------------------------------------------------------ auditoria

    @Test
    @Order(30)
    void laAuditoriaRegistroLaVenta() throws Exception {
        MvcResult resultado = mvc.perform(get("/api/auditoria?tabla=ventas")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accion").value("REGISTRAR_VENTA"))
                .andReturn();

        JsonNode registros = JSON.readTree(resultado.getResponse().getContentAsString());
        assertEquals(1, registros.size(), "Debe haber una sola venta auditada");
    }

    private String leer(MvcResult resultado, String campo) throws Exception {
        return JSON.readTree(resultado.getResponse().getContentAsString()).get(campo).asText();
    }
}
