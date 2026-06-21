package com.edu.sistema_inventario.repository;

import com.edu.sistema_inventario.model.EventoNegocio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventoNegocioRepository extends JpaRepository<EventoNegocio, Long> {
}
