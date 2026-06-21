package com.edu.sistema_inventario.service;

import com.edu.sistema_inventario.dto.UsuarioCreateRequest;
import com.edu.sistema_inventario.dto.UsuarioUpdateRequest;
import com.edu.sistema_inventario.model.Rol;
import com.edu.sistema_inventario.model.Usuario;
import com.edu.sistema_inventario.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {
    
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public Usuario guardarUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
    
    public Usuario obtenerUsuarioPorId(Long id) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        return usuario.orElse(null);
    }
    
    public Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
    
    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }
    
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }
    
    public Usuario actualizarUsuario(Long id, UsuarioUpdateRequest usuarioActualizado) {
        Optional<Usuario> usuarioExistente = usuarioRepository.findById(id);
        if (usuarioExistente.isPresent()) {
            Usuario usuario = usuarioExistente.get();
            if (usuarioActualizado.getNombre() != null && !usuarioActualizado.getNombre().isBlank()) {
                usuario.setNombre(usuarioActualizado.getNombre());
            }
            if (usuarioActualizado.getApellido() != null && !usuarioActualizado.getApellido().isBlank()) {
                usuario.setApellido(usuarioActualizado.getApellido());
            }
            if (usuarioActualizado.getEmail() != null && !usuarioActualizado.getEmail().isBlank()) {
                if (!usuario.getEmail().equalsIgnoreCase(usuarioActualizado.getEmail())
                        && usuarioRepository.existsByEmail(usuarioActualizado.getEmail())) {
                    throw new IllegalArgumentException("El email ya esta registrado");
                }
                usuario.setEmail(usuarioActualizado.getEmail());
            }
            if (usuarioActualizado.getPassword() != null && !usuarioActualizado.getPassword().isBlank()) {
                usuario.setPassword(passwordEncoder.encode(usuarioActualizado.getPassword()));
            }
            if (usuarioActualizado.getRol() != null && !usuarioActualizado.getRol().isBlank()) {
                usuario.setRol(parseRol(usuarioActualizado.getRol()));
            }
            if (usuarioActualizado.getActivo() != null) {
                usuario.setActivo(usuarioActualizado.getActivo());
            }
            return usuarioRepository.save(usuario);
        }
        return null;
    }
    
    public boolean eliminarUsuario(Long id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    // Métodos heredados para compatibilidad
    public Usuario registrar(UsuarioCreateRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya esta registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(parseRol(request.getRol()));
        usuario.setActivo(request.getActivo() == null ? true : request.getActivo());
        return guardarUsuario(usuario);
    }
    
    public List<Usuario> listar() {
        return obtenerTodosLosUsuarios();
    }
    
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }
    
    public boolean eliminar(Long id) {
        return eliminarUsuario(id);
    }

    private Rol parseRol(String rol) {
        try {
            return Rol.valueOf(rol.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Rol invalido");
        }
    }
}
