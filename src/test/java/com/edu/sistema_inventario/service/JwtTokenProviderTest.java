package com.edu.sistema_inventario.service;

import com.edu.sistema_inventario.config.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "esta-es-una-clave-secreta-larga-para-test-1234567890!");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 900000L);
    }

    @Test
    void generateToken_DebeGenerarTokenExitosamente() {
        String token = jwtTokenProvider.generateToken("test@test.com", "ADMIN", 99L);
        
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("test@test.com", jwtTokenProvider.getUsernameFromToken(token));
        assertEquals("ADMIN", jwtTokenProvider.getRoleFromToken(token));
        assertEquals(99L, jwtTokenProvider.getUserIdFromToken(token));
    }
}
