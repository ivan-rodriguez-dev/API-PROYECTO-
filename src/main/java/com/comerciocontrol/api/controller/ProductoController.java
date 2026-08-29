package com.comerciocontrol.api.controller;

import com.comerciocontrol.api.dto.ProductoRequest;
import com.comerciocontrol.api.exception.RespuestaError;
import com.comerciocontrol.api.model.Producto;
import com.comerciocontrol.api.security.RolesPermitidos;
import com.comerciocontrol.api.security.UsuarioActual;
import com.comerciocontrol.api.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

/** Servicios web del modulo de inventario. */
@RestController
@RequestMapping("/api/productos")
@Tag(name = "02 - Inventario", description = "Gestion del catalogo de productos y control de stock")
public class ProductoController {

    private final ProductoService servicio;
    private final UsuarioActual usuarioActual;

    public ProductoController(ProductoService servicio, UsuarioActual usuarioActual) {
        this.servicio = servicio;
        this.usuarioActual = usuarioActual;
    }

    @GetMapping
    @Operation(summary = "Listar productos",
            description = "Devuelve el catalogo con filtros opcionales por texto y por categoria. "
                    + "Cada producto incluye los campos calculados 'estado' (OK / CRITICO) y "
                    + "'utilidadUnitaria'.")
    public List<Producto> listar(
            @Parameter(description = "Texto a buscar en el codigo o el nombre", example = "arroz")
            @RequestParam(required = false) String q,
            @Parameter(description = "Categoria exacta", example = "Granos")
            @RequestParam(required = false) String categoria,
            @Parameter(description = "Incluir tambien los productos dados de baja")
            @RequestParam(defaultValue = "false") boolean incluirInactivos) {
        return servicio.listar(q, categoria, incluirInactivos);
    }

    @GetMapping("/stock-critico")
    @Operation(summary = "Productos con stock critico",
            description = "Productos cuyo stock actual esta en el minimo o por debajo. "
                    + "Alimenta la alerta de reposicion del dashboard.")
    public List<Producto> stockCritico() {
        return servicio.stockCritico();
    }

    @GetMapping("/categorias")
    @Operation(summary = "Listar categorias existentes")
    public List<String> categorias() {
        return servicio.categorias();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un producto por su id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "El producto no existe",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    public Producto obtener(@PathVariable int id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @RolesPermitidos({"administrador", "bodeguero"})
    @Operation(summary = "Crear un producto",
            description = "Valida que el codigo sea unico, que el precio de venta no sea menor "
                    + "que el de costo y que el proveedor exista.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto creado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "403", description = "El rol no tiene permiso",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "409", description = "Codigo duplicado o regla de negocio incumplida",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    public ResponseEntity<Producto> crear(@Valid @RequestBody ProductoRequest peticion) {
        Producto creado = servicio.crear(peticion, usuarioActual.id());
        return ResponseEntity.created(URI.create("/api/productos/" + creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    @RolesPermitidos({"administrador", "bodeguero"})
    @Operation(summary = "Actualizar un producto")
    public Producto actualizar(@PathVariable int id, @Valid @RequestBody ProductoRequest peticion) {
        return servicio.actualizar(id, peticion, usuarioActual.id());
    }

    @DeleteMapping("/{id}")
    @RolesPermitidos({"administrador"})
    @Operation(summary = "Dar de baja un producto",
            description = "Baja logica: el producto deja de estar disponible para la venta pero "
                    + "conserva su historial de ventas y movimientos.")
    @ApiResponse(responseCode = "204", description = "Producto dado de baja")
    public ResponseEntity<Void> desactivar(@PathVariable int id) {
        servicio.desactivar(id, usuarioActual.id());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
