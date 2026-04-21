package com.edu.sistema_inventario.service;

import com.edu.sistema_inventario.model.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UsuarioService {

    private final Map<Long, Usuario> usuarios = new ConcurrentHashMap<>();
    private final AtomicLong secuenciaId = new AtomicLong(0);
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario registrar(Usuario usuario) {
        validarUsuario(usuario);
        if (buscarPorEmail(usuario.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con este email");
        }

        Long id = secuenciaId.incrementAndGet();
        usuario.setId(id);
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuarios.put(id, usuario);
        return usuario;
    }

    public List<Usuario> listar() {
        return new ArrayList<>(usuarios.values());
    }

    public Optional<Usuario> buscarPorId(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return Optional.ofNullable(usuarios.get(id));
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return usuarios.values().stream()
                .filter(usuario -> email.equalsIgnoreCase(usuario.getEmail()))
                .findFirst();
    }

    public boolean eliminar(Long id) {
        if (id == null || id <= 0) {
            return false;
        }
        return usuarios.remove(id) != null;
    }

    private void validarUsuario(Usuario usuario) {
        if (usuario == null
                || usuario.getNombre() == null || usuario.getNombre().isBlank()
                || usuario.getApellido() == null || usuario.getApellido().isBlank()
                || usuario.getEmail() == null || usuario.getEmail().isBlank()
                || usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            throw new IllegalArgumentException("Datos inválidos");
        }
    }
}
