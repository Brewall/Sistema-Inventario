package com.edu.sistema_inventario.controller;

import com.edu.sistema_inventario.model.Categoria;
import com.edu.sistema_inventario.service.CategoriaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Categoria categoria) {
        if (!esValida(categoria)) {
            return ResponseEntity.badRequest().body("Datos inválidos: nombreCategoria, descripcionCategoria y estado son obligatorios");
        }

        try {
            Categoria creada = categoriaService.crear(categoria);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Categoria>> listar() {
        return ResponseEntity.ok(categoriaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Categoria> buscarPorId(@PathVariable Long id) {
        return categoriaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private boolean esValida(Categoria categoria) {
        return categoria != null
                && categoria.getNombreCategoria() != null
                && !categoria.getNombreCategoria().isBlank()
                && categoria.getDescripcionCategoria() != null
                && !categoria.getDescripcionCategoria().isBlank()
                && categoria.getEstado() != null;
    }
}
