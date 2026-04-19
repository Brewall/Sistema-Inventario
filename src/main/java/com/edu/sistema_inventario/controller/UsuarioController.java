package com.edu.sistema_inventario.controller;

import com.edu.sistema_inventario.model.Usuario;
import com.edu.sistema_inventario.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody Usuario usuario) {
        if (!esValido(usuario)) {
            return ResponseEntity.badRequest().body("Datos inválidos: nombre, apellido, email y password son obligatorios");
        }

        try {
            Usuario registrado = usuarioService.registrar(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(registrado);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable("id") Long id) {
        return usuarioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        boolean eliminado = usuarioService.eliminar(id);
        return eliminado ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    private boolean esValido(Usuario usuario) {
        return usuario != null
                && usuario.getNombre() != null
                && !usuario.getNombre().isBlank()
                && usuario.getApellido() != null
                && !usuario.getApellido().isBlank()
                && usuario.getEmail() != null
                && !usuario.getEmail().isBlank()
                && usuario.getPassword() != null
                && !usuario.getPassword().isBlank();
    }
}
