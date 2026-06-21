package com.edu.sistema_inventario.service;

import com.edu.sistema_inventario.model.EventoNegocio;
import com.edu.sistema_inventario.repository.EventoNegocioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditoriaNegocioService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaNegocioService.class);
    private final EventoNegocioRepository repository;

    public AuditoriaNegocioService(EventoNegocioRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void registrarEvento(String tipoEvento, String descripcion) {
        // Obtener usuario del MDC, o en su defecto del SecurityContext, o "system"
        String usuario = MDC.get("usuario");
        if (usuario == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null) {
                usuario = auth.getName();
            } else {
                usuario = "system";
            }
        }

        // Logger tradicional (utilizará MDC si el log pattern está configurado para ello)
        log.info("[EVENTO: {}] [USUARIO: {}] - {}", tipoEvento, usuario, descripcion);

        // Guardar en la base de datos
        EventoNegocio evento = new EventoNegocio(tipoEvento, descripcion, usuario);
        repository.save(evento);
    }
}
