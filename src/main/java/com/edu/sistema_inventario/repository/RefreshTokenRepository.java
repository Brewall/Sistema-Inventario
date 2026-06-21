package com.edu.sistema_inventario.repository;

import com.edu.sistema_inventario.model.RefreshToken;
import com.edu.sistema_inventario.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteAllByUsuario(Usuario usuario);
    void deleteAllByExpiryDateBeforeOrRevokedTrue(java.time.Instant expiryDate);
}
