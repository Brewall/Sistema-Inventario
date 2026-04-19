package com.edu.sistema_inventario.controller;

import com.edu.sistema_inventario.model.Producto;
import com.edu.sistema_inventario.service.ProductoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<List<Producto>> listarProductos() {
        return ResponseEntity.ok(productoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerProductoPorId(@PathVariable Long id) {
        return productoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> crearProducto(@RequestBody Producto producto) {
        if (!esValido(producto)) {
            return ResponseEntity.badRequest().body("Datos inválidos: nombre, precio y stock son obligatorios");
        }

        try {
            Producto creado = productoService.crear(producto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProducto(@PathVariable Long id, @RequestBody Producto producto) {
        if (!esValido(producto)) {
            return ResponseEntity.badRequest().body("Datos inválidos: nombre, precio y stock son obligatorios");
        }

        try {
            return productoService.actualizar(id, producto)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<?> actualizarStock(@PathVariable Long id, @RequestParam Integer stock) {
        if (stock == null || stock < 0) {
            return ResponseEntity.badRequest().body("Stock inválido: debe ser un número mayor o igual a cero");
        }

        try {
            return productoService.actualizarStock(id, stock)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        boolean eliminado = productoService.eliminar(id);
        return eliminado ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Producto>> buscarPorNombre(@RequestParam(required = false) String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(productoService.buscarPorNombre(nombre));
    }

    @GetMapping("/stock-bajo")
    public ResponseEntity<List<Producto>> listarStockBajo(@RequestParam(required = false) Integer umbral) {
        if (umbral == null || umbral < 0) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(productoService.listarStockBajo(umbral));
    }

    private boolean esValido(Producto producto) {
        return producto != null
                && producto.getNombre() != null
                && !producto.getNombre().isBlank()
                && producto.getPrecio() != null
                && producto.getPrecio() >= 0
                && producto.getStock() != null
                && producto.getStock() >= 0;
    }
}
