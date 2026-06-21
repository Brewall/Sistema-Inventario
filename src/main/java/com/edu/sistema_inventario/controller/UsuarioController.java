package com.edu.sistema_inventario.controller;

import com.edu.sistema_inventario.dto.UsuarioCreateRequest;
import com.edu.sistema_inventario.dto.UsuarioResponse;
import com.edu.sistema_inventario.dto.UsuarioUpdateRequest;
import com.edu.sistema_inventario.exception.ApiException;
import com.edu.sistema_inventario.model.Usuario;
import com.edu.sistema_inventario.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Endpoints para gestion de usuarios (solo ADMIN)")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Listar todos los usuarios",
            description = "Obtiene la lista de todos los usuarios del sistema. Solo administradores pueden acceder.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida"),
                    @ApiResponse(responseCode = "403", description = "No tiene permisos ADMIN")
            }
    )
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(
                usuarioService.listar().stream().map(this::toResponse).collect(Collectors.toList())
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Obtener usuario por ID",
            description = "Obtiene los detalles de un usuario especifico. Solo administradores pueden acceder.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
                    @ApiResponse(responseCode = "403", description = "No tiene permisos ADMIN"),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
            }
    )
    public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable("id") Long id) {
        return usuarioService.buscarPorId(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Crear nuevo usuario (ADMIN)",
            description = "Crea un nuevo usuario en el sistema. Esta operacion es para administradores. El registro publico es en /auth/register.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos invalidos"),
                    @ApiResponse(responseCode = "403", description = "No tiene permisos ADMIN")
            }
    )
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody UsuarioCreateRequest usuario) {
        Usuario registrado = usuarioService.registrar(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(registrado));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> actualizarUsuario(@PathVariable("id") Long id, @Valid @RequestBody UsuarioUpdateRequest usuario) {
        Usuario actualizado = usuarioService.actualizarUsuario(id, usuario);
        if (actualizado != null) {
            return ResponseEntity.ok(toResponse(actualizado));
        }
        throw new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        boolean eliminado = usuarioService.eliminar(id);
        if (eliminado) {
            return ResponseEntity.noContent().build();
        }
        throw new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getRol() == null ? null : usuario.getRol().name(),
                usuario.getCreatedAt(),
                usuario.getActivo()
        );
    }
}
