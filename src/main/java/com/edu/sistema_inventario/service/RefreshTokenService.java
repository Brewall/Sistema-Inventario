package com.edu.sistema_inventario.service;

import com.edu.sistema_inventario.model.RefreshToken;
import com.edu.sistema_inventario.model.Usuario;
import com.edu.sistema_inventario.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration-days:7}")
    private long refreshExpirationDays;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshToken createRefreshToken(Usuario usuario) {
        RefreshToken token = new RefreshToken();
        token.setUsuario(usuario);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(Instant.now().plus(refreshExpirationDays, ChronoUnit.DAYS));
        token.setRevoked(false);
        return refreshTokenRepository.save(token);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public boolean isValid(RefreshToken token) {
        return token != null && !token.isRevoked() && token.getExpiryDate().isAfter(Instant.now());
    }

    public void revoke(RefreshToken token) {
        if (token != null) {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        }
    }

    public void revokeAllForUser(Usuario usuario) {
        refreshTokenRepository.deleteAllByUsuario(usuario);
    }
    
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.scheduling.annotation.Scheduled(cron = "${jwt.refresh-cleanup-cron:0 0 2 * * ?}")
    public void purgeExpiredOrRevokedTokens() {
        refreshTokenRepository.deleteAllByExpiryDateBeforeOrRevokedTrue(Instant.now());
    }
}
