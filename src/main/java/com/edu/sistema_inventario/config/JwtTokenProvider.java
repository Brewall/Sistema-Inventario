package com.edu.sistema_inventario.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret:mi-clave-secreta-muy-segura-para-desarrollo-sistema-inventario}")
    private String jwtSecret;
    
    @Value("${jwt.expiration:900000}")
    private long jwtExpirationMs;
    
    public String generateToken(String email, String rol, Long userId) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        
        return Jwts.builder()
                .subject(email)
                .claim("rol", rol)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key)
                .compact();
    }
    
    public String getUsernameFromToken(String token) {
        return getTokenBody(token).getSubject();
    }
    
    public String getRoleFromToken(String token) {
        return (String) getTokenBody(token).get("rol");
    }

    public Long getUserIdFromToken(String token) {
        Object userId = getTokenBody(token).get("userId");
        if (userId instanceof Number number) {
            return number.longValue();
        }
        if (userId instanceof String text && !text.isBlank()) {
            return Long.valueOf(text);
        }
        return null;
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    private Claims getTokenBody(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
