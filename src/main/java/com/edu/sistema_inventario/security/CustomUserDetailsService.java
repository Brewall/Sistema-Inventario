package com.edu.sistema_inventario.security;

import com.edu.sistema_inventario.model.Usuario;
import com.edu.sistema_inventario.service.UsuarioService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioService usuarioService;

    public CustomUserDetailsService(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioService.buscarPorEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        String roleCode = usuario.getRol() != null && usuario.getRol().getCodigo() != null
                ? usuario.getRol().getCodigo()
                : "USER";

        String authority = roleCode.startsWith("ROLE_") ? roleCode : "ROLE_" + roleCode;

        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority(authority)))
                .build();
    }
}
