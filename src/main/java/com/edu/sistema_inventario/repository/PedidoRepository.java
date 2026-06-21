package com.edu.sistema_inventario.repository;

import com.edu.sistema_inventario.model.Pedido;
import com.edu.sistema_inventario.model.PedidoEstado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByUsuarioId(Long usuarioId);

    Page<Pedido> findAll(Pageable pageable);
    Page<Pedido> findByUsuarioId(Long usuarioId, Pageable pageable);
    Page<Pedido> findByEstado(PedidoEstado estado, Pageable pageable);
    Page<Pedido> findByFechaBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);
}
