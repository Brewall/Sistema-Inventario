package com.edu.sistema_inventario.controller;

import com.edu.sistema_inventario.config.JwtTokenProvider;
import com.edu.sistema_inventario.model.RefreshToken;
import com.edu.sistema_inventario.model.Rol;
import com.edu.sistema_inventario.model.Usuario;
import com.edu.sistema_inventario.service.RefreshTokenService;
import com.edu.sistema_inventario.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerRefreshLogoutTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        var controller = new AuthController(usuarioService, jwtTokenProvider, passwordEncoder, refreshTokenService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new com.edu.sistema_inventario.exception.GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void login_deberiaRetornarUserId_y_refreshToken() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(7L);
        usuario.setEmail("u@demo.com");
        usuario.setPassword("hashed");
        usuario.setActivo(true);
        usuario.setRol(Rol.USER);

        RefreshToken rt = new RefreshToken();
        rt.setToken("rt-789");
        rt.setUsuario(usuario);
        rt.setExpiryDate(Instant.now().plusSeconds(3600));

        when(usuarioService.findByEmail("u@demo.com")).thenReturn(usuario);
        when(passwordEncoder.matches("secret123", "hashed")).thenReturn(true);
        when(jwtTokenProvider.generateToken("u@demo.com", "USER", 7L)).thenReturn("access-xyz");
        when(refreshTokenService.createRefreshToken(usuario)).thenReturn(rt);

        var body = """
                {"email":"u@demo.com","password":"secret123"}
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-xyz"))
                .andExpect(jsonPath("$.userId").value(7))
                .andExpect(jsonPath("$.email").value("u@demo.com"))
                .andExpect(jsonPath("$.rol").value("USER"))
                .andExpect(jsonPath("$.refreshToken").value("rt-789"));
    }

    @Test
    void refreshToken_deberiaRetornarTokenNuevo_siRefreshValido() throws Exception {
        RefreshToken rt = new RefreshToken();
        rt.setToken("rt-123");
        Usuario usuario = new Usuario();
        usuario.setId(8L);
        usuario.setEmail("u@demo.com");
        usuario.setRol(Rol.USER);
        rt.setUsuario(usuario);
        rt.setExpiryDate(Instant.now().plusSeconds(3600));

        when(refreshTokenService.findByToken(anyString())).thenReturn(Optional.of(rt));
        when(refreshTokenService.isValid(rt)).thenReturn(true);
        when(jwtTokenProvider.generateToken(usuario.getEmail(), usuario.getRol().name(), usuario.getId())).thenReturn("access-abc");

        var body = objectMapper.writeValueAsString(new com.edu.sistema_inventario.dto.RefreshRequest("rt-123"));

        mockMvc.perform(post("/auth/refresh")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-abc"));
    }

    @Test
    void logout_deberiaRevocarRefreshToken_yRetornar204() throws Exception {
        RefreshToken rt = new RefreshToken();
        rt.setToken("rt-456");
        Usuario usuario = new Usuario();
        usuario.setEmail("u2@demo.com");
        usuario.setRol(Rol.USER);
        rt.setUsuario(usuario);

        when(refreshTokenService.findByToken(anyString())).thenReturn(Optional.of(rt));

        var body = objectMapper.writeValueAsString(new com.edu.sistema_inventario.dto.RefreshRequest("rt-456"));

        mockMvc.perform(post("/auth/logout")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isNoContent());

        verify(refreshTokenService).findByToken("rt-456");
        verify(refreshTokenService).revoke(rt);
    }
}
