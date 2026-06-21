package com.edu.sistema_inventario.service;

import com.edu.sistema_inventario.dto.UsuarioUpdateRequest;
import com.edu.sistema_inventario.model.Rol;
import com.edu.sistema_inventario.model.Usuario;
import com.edu.sistema_inventario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void guardarUsuario_DebeGuardarExitosamente() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Juan");
        usuario.setApellido("Perez");
        usuario.setEmail("juan@test.com");
        usuario.setPassword("12345");
        usuario.setRol(Rol.USER);

        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario resultado = usuarioService.guardarUsuario(usuario);

        assertNotNull(resultado);
        assertEquals(Rol.USER, resultado.getRol());
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    void actualizarUsuario_DebeActualizarYRetornarUsuario_CuandoExiste() {
        Usuario existente = new Usuario();
        existente.setId(1L);
        existente.setNombre("Viejo");

        UsuarioUpdateRequest actualizado = new UsuarioUpdateRequest();
        actualizado.setNombre("Nuevo");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(existente);

        Usuario resultado = usuarioService.actualizarUsuario(1L, actualizado);

        assertNotNull(resultado);
        assertEquals("Nuevo", resultado.getNombre());
        verify(usuarioRepository, times(1)).save(existente);
    }
}
