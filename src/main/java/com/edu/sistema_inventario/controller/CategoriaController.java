package com.edu.sistema_inventario.controller;

import com.edu.sistema_inventario.model.Categoria;
import com.edu.sistema_inventario.exception.ApiException;
import com.edu.sistema_inventario.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorías", description = "Endpoints para gestión de categorías de productos")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    // 1. Crear (Solo ADMIN)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Crear nueva categoría",
            description = "Crea una nueva categoría de productos. Solo administradores pueden crear.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Categoría creada exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "403", description = "No tiene permisos ADMIN")
            }
    )
    public ResponseEntity<?> crear(@RequestBody Categoria categoria) {
        if (!esValida(categoria)) {
            throw new IllegalArgumentException("Datos inválidos: nombre y estado son obligatorios");
        }
        Categoria creada = categoriaService.crear(categoria);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    // 2. Listar
    @GetMapping
    @Operation(
            summary = "Listar todas las categorías",
            description = "Obtiene la lista de todas las categorías disponibles",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de categorías obtenida exitosamente")
            }
    )
    public ResponseEntity<List<Categoria>> listar() {
        return ResponseEntity.ok(categoriaService.listar());
    }

    // 3. Buscar por ID
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener categoría por ID",
            description = "Obtiene los detalles de una categoría específica",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Categoría encontrada"),
                    @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
            }
    )
    public ResponseEntity<Categoria> buscarPorId(@PathVariable("id") Long id) {
        Categoria cat = categoriaService.buscarPorId(id).orElse(null);
        if (cat != null) {
            return ResponseEntity.ok(cat);
        }
        throw new ApiException(HttpStatus.NOT_FOUND, "Categoría no encontrada");
    }
    
    // 4. Actualizar (Solo ADMIN)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Actualizar categoría",
            description = "Actualiza los detalles de una categoría existente. Solo administradores pueden actualizar.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Categoría actualizada exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "403", description = "No tiene permisos ADMIN"),
                    @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
            }
    )
    public ResponseEntity<?> actualizar(@PathVariable("id") Long id, @RequestBody Categoria categoriaActualizada) {
        if (!esValida(categoriaActualizada)) {
            throw new IllegalArgumentException("Datos inválidos: nombre y estado son obligatorios");
        }
        Categoria existente = categoriaService.buscarPorId(id).orElse(null);
        if (existente == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Categoría no encontrada");
        }
        
        // Simular update ya que falta metodo en categoriaService (lo podemos actualizar directo o el respository)
        existente.setNombre(categoriaActualizada.getNombre());
        existente.setDescripcion(categoriaActualizada.getDescripcion());
        existente.setEstado(categoriaActualizada.getEstado());
        // En una implementacion real se actualiza mediante un servicio. Llama al crear para guardar si es jpa repository
        Categoria guardada = categoriaService.crear(existente);
        return ResponseEntity.ok(guardada);
    }
    
    // 5. Eliminar (Solo ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Eliminar categoría",
            description = "Elimina una categoría del sistema. Solo administradores pueden eliminar.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Categoría eliminada exitosamente"),
                    @ApiResponse(responseCode = "403", description = "No tiene permisos ADMIN"),
                    @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
            }
    )
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        boolean eliminado = categoriaService.eliminar(id);
        if (eliminado) {
            return ResponseEntity.noContent().build();
        }
        throw new ApiException(HttpStatus.NOT_FOUND, "Categoría no encontrada");
    }

    private boolean esValida(Categoria categoria) {
        return categoria != null
                && categoria.getNombre() != null
                && !categoria.getNombre().isBlank();
    }
}
