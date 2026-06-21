package com.edu.sistema_inventario.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
public class VersionDataNormalizer {

    private static final Logger log = LoggerFactory.getLogger(VersionDataNormalizer.class);

    @PersistenceContext
    private EntityManager entityManager;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void normalizeNullVersions() {
        int productos = normalize("productos");
        int pedidos = normalize("pedidos");

        if (productos + pedidos > 0) {
            log.info("Normalizacion de version completada. productos={}, pedidos={}",
                    productos, pedidos);
        }
    }

    private int normalize(String tableName) {
        return entityManager.createNativeQuery("UPDATE " + tableName + " SET version = 0 WHERE version IS NULL")
                .executeUpdate();
    }
}
