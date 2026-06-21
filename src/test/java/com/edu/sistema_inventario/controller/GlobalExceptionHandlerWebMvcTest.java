package com.edu.sistema_inventario.controller;

import com.edu.sistema_inventario.config.JwtTokenProvider;
import com.edu.sistema_inventario.dto.RegisterRequest;
import com.edu.sistema_inventario.exception.GlobalExceptionHandler;
import com.edu.sistema_inventario.service.RefreshTokenService;
import com.edu.sistema_inventario.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerWebMvcTest {

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
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        globalExceptionHandler = new GlobalExceptionHandler();
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new AuthController(usuarioService, jwtTokenProvider, passwordEncoder, refreshTokenService))
                .setControllerAdvice(globalExceptionHandler)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void register_deberiaRetornarProblemDetails_cuandoEmailYaExiste() throws Exception {
        RegisterRequest request = new RegisterRequest("Ana", "Perez", "ana@demo.com", "secreto1");
        when(usuarioService.existsByEmail(anyString())).thenReturn(true);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.detail").value("El email ya está registrado"));
    }

    @Test
    void handleValidation_deberiaIncluirInvalidParams() throws Exception {
        RegisterRequest request = new RegisterRequest();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(request, "registerRequest");
        bindingResult.rejectValue("email", "NotBlank", "El email es obligatorio");
        bindingResult.rejectValue("password", "NotBlank", "La contraseña es obligatoria");

        Method method = AuthController.class.getMethod("register", RegisterRequest.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

        var problemDetail = globalExceptionHandler.handleValidation(exception);

        org.junit.jupiter.api.Assertions.assertEquals(400, problemDetail.getStatus());
        org.junit.jupiter.api.Assertions.assertEquals("Error de validacion en los datos", problemDetail.getTitle());
        org.junit.jupiter.api.Assertions.assertTrue(problemDetail.getProperties().containsKey("invalidParams"));
        org.junit.jupiter.api.Assertions.assertTrue(problemDetail.getProperties().containsKey("message"));
    }
}
