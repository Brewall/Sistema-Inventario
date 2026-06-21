package com.edu.sistema_inventario.controller;

import com.edu.sistema_inventario.config.JwtTokenProvider;
import com.edu.sistema_inventario.dto.AuthResponse;
import com.edu.sistema_inventario.dto.RefreshRequest;
import com.edu.sistema_inventario.dto.RefreshResponse;
import com.edu.sistema_inventario.dto.LoginRequest;
import com.edu.sistema_inventario.dto.RegisterRequest;
import com.edu.sistema_inventario.exception.ApiException;
import com.edu.sistema_inventario.model.Rol;
import com.edu.sistema_inventario.model.Usuario;
import com.edu.sistema_inventario.service.UsuarioService;
import com.edu.sistema_inventario.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Endpoints para registro y login de usuarios")
public class AuthController {
    
    private final UsuarioService usuarioService;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    
    public AuthController(UsuarioService usuarioService, JwtTokenProvider tokenProvider, 
                         PasswordEncoder passwordEncoder, RefreshTokenService refreshTokenService) {
        this.usuarioService = usuarioService;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }
    
    @PostMapping("/register")
    @Operation(
            summary = "Registrar nuevo usuario",
            description = "Crea un nuevo usuario en el sistema. El usuario es creado con rol USER por defecto.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuario registrado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Email ya registrado o datos inválidos"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            }
    )
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (usuarioService.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "El email ya está registrado");
        }

        Usuario usuario = new Usuario(
            request.getNombre(),
            request.getApellido(),
            request.getEmail(),
            passwordEncoder.encode(request.getPassword()),
            Rol.USER
        );

        usuarioService.guardarUsuario(usuario);

        return ResponseEntity.ok()
            .body(new SuccessResponse("Usuario registrado exitosamente"));
    }
    
    @PostMapping("/login")
    @Operation(
            summary = "Login de usuario",
            description = "Autentica un usuario y devuelve un token JWT válido por 15 minutos",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login exitoso, retorna token JWT"),
                    @ApiResponse(responseCode = "401", description = "Email o contraseña inválidos"),
                    @ApiResponse(responseCode = "403", description = "Usuario inactivo"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            }
    )
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Usuario usuario = usuarioService.findByEmail(request.getEmail());

        if (usuario == null || !passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Email o contraseña inválidos");
        }

        if (!usuario.getActivo()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Usuario inactivo");
        }

        String token = tokenProvider.generateToken(usuario.getEmail(), usuario.getRol().name(), usuario.getId());
        // crear refresh token persistente
        var refresh = refreshTokenService.createRefreshToken(usuario);

        return ResponseEntity.ok()
            .body(new AuthResponse(token, usuario.getId(), usuario.getEmail(), usuario.getRol().name(), refresh.getToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refreshToken(@RequestBody RefreshRequest request) {
        String rtoken = request.getRefreshToken();
        if (rtoken == null || rtoken.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Refresh token inválido");
        }

        var maybe = refreshTokenService.findByToken(rtoken);
        if (maybe.isEmpty() || !refreshTokenService.isValid(maybe.get())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token inválido o expirado");
        }

        var usuario = maybe.get().getUsuario();
        String token = tokenProvider.generateToken(usuario.getEmail(), usuario.getRol().name(), usuario.getId());
        return ResponseEntity.ok(new RefreshResponse(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody RefreshRequest request) {
        String rtoken = request.getRefreshToken();
        if (rtoken != null) {
            refreshTokenService.findByToken(rtoken).ifPresent(refreshTokenService::revoke);
        }
        return ResponseEntity.noContent().build();
    }
    
    // Clases internas para respuestas
    public static class ErrorResponse {
        private String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
        
        public String getError() {
            return error;
        }
    }
    
    public static class SuccessResponse {
        private String mensaje;
        
        public SuccessResponse(String mensaje) {
            this.mensaje = mensaje;
        }
        
        public String getMensaje() {
            return mensaje;
        }
    }
}
