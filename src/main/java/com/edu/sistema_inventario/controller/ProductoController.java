package com.edu.sistema_inventario.controller;

import com.edu.sistema_inventario.model.Producto;
import com.edu.sistema_inventario.exception.ApiException;
import com.edu.sistema_inventario.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Endpoints para gestión de productos del inventario")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // 1. Listar (Todos)
    @GetMapping
    @Operation(
            summary = "Listar todos los productos",
            description = "Retorna una lista de todos los productos disponibles en el inventario, opcionalmente filtrados por categoría",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de productos obtenida exitosamente")
            }
    )
    @io.swagger.v3.oas.annotations.Parameters({
            @Parameter(name = "categoriaId", in = ParameterIn.QUERY, description = "ID de la categoría para filtrar"),
            @Parameter(name = "page", in = ParameterIn.QUERY, description = "Page index (0..N)"),
            @Parameter(name = "size", in = ParameterIn.QUERY, description = "Page size"),
            @Parameter(name = "sort", in = ParameterIn.QUERY, description = "Sort criteria e.g. field,asc")
    })
    public ResponseEntity<Page<Producto>> listarProductos(
            @RequestParam(value = "categoriaId", required = false) @Parameter(description = "ID de la categoría para filtrar") Long categoriaId,
            Pageable pageable) {
        if (categoriaId != null) {
            return ResponseEntity.ok(productoService.obtenerProductosPorCategoria(categoriaId, pageable));
        }
        return ResponseEntity.ok(productoService.obtenerTodosLosProductos(pageable));
    }

    // 2. Obtener por ID
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener producto por ID",
            description = "Obtiene los detalles de un producto específico",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Producto encontrado"),
                    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
            }
    )
    public ResponseEntity<Producto> obtenerProductoPorId(@PathVariable("id") Long id) {
        Producto producto = productoService.obtenerProductoPorId(id);
        if (producto != null) {
            return ResponseEntity.ok(producto);
        }
        throw new ApiException(HttpStatus.NOT_FOUND, "Producto no encontrado");
    }

    // 3. Crear (Solo ADMIN)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Crear nuevo producto",
            description = "Crea un nuevo producto en el inventario. Solo administradores pueden crear productos.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Producto creado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "403", description = "No tiene permisos ADMIN")
            }
    )
    public ResponseEntity<?> crearProducto(@RequestBody Producto producto) {
        if (!esValido(producto)) {
            throw new IllegalArgumentException("Datos inválidos: nombre, precio, stock y categoriaId son obligatorios");
        }
        Producto creado = productoService.guardarProducto(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // 4. Actualizar (Solo ADMIN)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Actualizar producto",
            description = "Actualiza los detalles de un producto existente. Solo administradores pueden actualizar.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "403", description = "No tiene permisos ADMIN"),
                    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
            }
    )
    public ResponseEntity<?> actualizarProducto(@PathVariable("id") Long id, @RequestBody Producto producto) {
        Producto actualizado = productoService.actualizarProducto(id, producto);
        if (actualizado != null) {
            return ResponseEntity.ok(actualizado);
        }
        throw new ApiException(HttpStatus.NOT_FOUND, "Producto no encontrado");
    }

    // 5. Eliminar (Solo ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Eliminar producto",
            description = "Elimina un producto del inventario. Solo administradores pueden eliminar.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Producto eliminado exitosamente"),
                    @ApiResponse(responseCode = "403", description = "No tiene permisos ADMIN"),
                    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
            }
    )
    public ResponseEntity<Void> eliminarProducto(@PathVariable("id") Long id) {
        boolean eliminado = productoService.eliminarProducto(id);
        if (!eliminado) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Producto no encontrado o ya eliminado");
        }
        return ResponseEntity.noContent().build();
    }

    // 6. Buscar por nombre
    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar productos por nombre",
            description = "Busca productos por nombre (búsqueda parcial, case-insensitive)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Búsqueda completada"),
                    @ApiResponse(responseCode = "400", description = "Nombre de búsqueda vacío")
            }
    )
    public ResponseEntity<Page<Producto>> buscarPorNombre(
            @RequestParam(value = "nombre") @Parameter(description = "Nombre parcial para búsqueda", required = true) String nombre,
            @Parameter(name = "page", in = ParameterIn.QUERY, description = "Page index (0..N)") Integer page,
            @Parameter(name = "size", in = ParameterIn.QUERY, description = "Page size") Integer size,
            @Parameter(name = "sort", in = ParameterIn.QUERY, description = "Sort criteria e.g. field,asc") String sort,
            Pageable pageable) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de búsqueda es obligatorio");
        }
        return ResponseEntity.ok(productoService.buscarPorNombre(nombre, pageable));
    }

    // 7. Incrementar stock (Solo ADMIN)
    @PostMapping("/{id}/stock/incrementar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Incrementar stock de producto",
            description = "Aumenta el stock disponible de un producto. Solo administradores pueden realizar esta acción.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Stock incrementado"),
                    @ApiResponse(responseCode = "400", description = "Cantidad inválida"),
                    @ApiResponse(responseCode = "403", description = "No tiene permisos ADMIN"),
                    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
            }
    )
    public ResponseEntity<?> incrementarStock(@PathVariable("id") Long id, @RequestParam("cantidad") Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("Cantidad debe ser mayor a 0");
        }
        boolean exito = productoService.incrementarStock(id, cantidad);
        if (exito) {
            return ResponseEntity.ok(new SuccessResponse("Stock incrementado"));
        }
        throw new ApiException(HttpStatus.NOT_FOUND, "Producto no encontrado");
    }

    // 8. Decrementar stock (Solo ADMIN)
    @PostMapping("/{id}/stock/decrementar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Decrementar stock de producto",
            description = "Reduce el stock disponible de un producto. Solo administradores. Verifica que haya stock suficiente.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Stock decrementado"),
                    @ApiResponse(responseCode = "400", description = "Stock insuficiente o cantidad inválida"),
                    @ApiResponse(responseCode = "403", description = "No tiene permisos ADMIN"),
                    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
            }
    )
    public ResponseEntity<?> decrementarStock(@PathVariable("id") Long id, @RequestParam("cantidad") Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("Cantidad debe ser mayor a 0");
        }
        boolean exito = productoService.decrementarStock(id, cantidad);
        if (exito) {
            return ResponseEntity.ok(new SuccessResponse("Stock decrementado"));
        }
        throw new IllegalArgumentException("Stock insuficiente o producto no encontrado");
    }

    private boolean esValido(Producto producto) {
        return producto != null
                && producto.getNombre() != null && !producto.getNombre().isBlank()
                && producto.getPrecio() != null && producto.getPrecio().compareTo(BigDecimal.ZERO) >= 0
                && producto.getStock() != null && producto.getStock() >= 0
                && producto.getCategoriaId() != null;
    }

    public static class SuccessResponse {
        private String mensaje;
        public SuccessResponse(String mensaje) { this.mensaje = mensaje; }
        public String getMensaje() { return mensaje; }
    }
}