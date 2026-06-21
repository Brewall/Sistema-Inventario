package com.edu.sistema_inventario.service;

import com.edu.sistema_inventario.dto.PedidoCreateRequest;
import com.edu.sistema_inventario.dto.PedidoItemRequest;
import com.edu.sistema_inventario.model.Producto;
import com.edu.sistema_inventario.repository.PedidoRepository;
import com.edu.sistema_inventario.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@TestPropertySource(properties = "spring.sql.init.mode=never")
@Import(PedidoService.class)
class PedidoIntegrationTest {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @org.springframework.boot.test.mock.mockito.MockBean
    private AuditoriaNegocioService auditoriaNegocioService;

    @BeforeEach
    void clean() {
        pedidoRepository.deleteAll();
        productoRepository.deleteAll();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void crearPedido_restaStock_y_guardaPedido() {
        Producto p = new Producto("Prod A", "Desc", BigDecimal.valueOf(10.0), 10, 1L);
        productoRepository.save(p);

        PedidoItemRequest item = new PedidoItemRequest(p.getId(), 3);
        PedidoCreateRequest req = new PedidoCreateRequest(1L, List.of(item));

        var creado = pedidoService.guardarPedido(req);

        Producto actual = productoRepository.findById(p.getId()).orElseThrow();
        assertThat(actual.getStock()).isEqualTo(7);
        assertThat(creado.getId()).isNotNull();
        assertThat(creado.getItems()).hasSize(1);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void crearPedido_conFallo_deberiaRollbackCambiosDeStock() {
        Producto p = new Producto("Prod B", "Desc2", BigDecimal.valueOf(20.0), 5, 1L);
        productoRepository.save(p);

        // Second product does not exist -> will cause exception after first product save occurs inside method
        PedidoItemRequest good = new PedidoItemRequest(p.getId(), 2);
        PedidoItemRequest bad = new PedidoItemRequest(999999L, 1);
        PedidoCreateRequest req = new PedidoCreateRequest(2L, List.of(good, bad));

        assertThrows(IllegalArgumentException.class, () -> pedidoService.guardarPedido(req));

        Producto actual = productoRepository.findById(p.getId()).orElseThrow();
        // stock should be unchanged due to transaction rollback
        assertThat(actual.getStock()).isEqualTo(5);
        assertThat(pedidoRepository.findAll()).isEmpty();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void cancelarPedido_restauraStock_y_cambiaEstado() {
        Producto p = new Producto("Prod C", "Desc3", BigDecimal.valueOf(30.0), 10, 1L);
        productoRepository.save(p);

        PedidoItemRequest item = new PedidoItemRequest(p.getId(), 4);
        PedidoCreateRequest req = new PedidoCreateRequest(3L, List.of(item));

        var creado = pedidoService.guardarPedido(req);
        
        // Stock should be 6 after order creation
        Producto despuesCrear = productoRepository.findById(p.getId()).orElseThrow();
        assertThat(despuesCrear.getStock()).isEqualTo(6);

        // Cancel order
        var cancelado = pedidoService.actualizarEstadoPedido(creado.getId(), "CANCELADO");
        assertThat(cancelado.getEstado()).isEqualTo(com.edu.sistema_inventario.model.PedidoEstado.CANCELADO);

        // Stock should be restored to 10
        Producto despuesCancelar = productoRepository.findById(p.getId()).orElseThrow();
        assertThat(despuesCancelar.getStock()).isEqualTo(10);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void cancelarPedido_entregado_deberiaFallar() {
        Producto p = new Producto("Prod D", "Desc4", BigDecimal.valueOf(40.0), 10, 1L);
        productoRepository.save(p);

        PedidoItemRequest item = new PedidoItemRequest(p.getId(), 2);
        PedidoCreateRequest req = new PedidoCreateRequest(4L, List.of(item));

        var creado = pedidoService.guardarPedido(req);
        
        // Set state to ENTREGADO (finished)
        var entregado = pedidoService.actualizarEstadoPedido(creado.getId(), "ENTREGADO");
        assertThat(entregado.getEstado()).isEqualTo(com.edu.sistema_inventario.model.PedidoEstado.ENTREGADO);

        // Attempting to cancel should throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> 
            pedidoService.actualizarEstadoPedido(creado.getId(), "CANCELADO")
        );

        // Stock should remain at 8 (since ENTREGADO doesn't restore and CANCELADO failed)
        Producto finalProd = productoRepository.findById(p.getId()).orElseThrow();
        assertThat(finalProd.getStock()).isEqualTo(8);
    }
}
