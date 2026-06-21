package com.edu.sistema_inventario.service;

import com.edu.sistema_inventario.dto.PedidoCreateRequest;
import com.edu.sistema_inventario.dto.PedidoItemRequest;
import com.edu.sistema_inventario.model.Pedido;
import com.edu.sistema_inventario.model.PedidoEstado;
import com.edu.sistema_inventario.model.Producto;
import com.edu.sistema_inventario.repository.PedidoRepository;
import com.edu.sistema_inventario.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private AuditoriaNegocioService auditoriaNegocioService;

    @InjectMocks
    private PedidoService pedidoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void guardarPedido_DebeGuardarExitosamente() {
        Producto producto = new Producto();
        producto.setId(10L);
        producto.setPrecio(BigDecimal.valueOf(150.0));
        producto.setStock(5);
        producto.setActivo(true);

        PedidoCreateRequest request = new PedidoCreateRequest(1L, List.of(new PedidoItemRequest(10L, 1)));

        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pedido resultado = pedidoService.guardarPedido(request);

        assertNotNull(resultado);
        assertEquals(PedidoEstado.PENDIENTE, resultado.getEstado());
        assertEquals(BigDecimal.valueOf(150.0), resultado.getTotal());
        assertEquals(1, resultado.getItems().size());
        assertEquals(4, producto.getStock());
        assertNotNull(resultado.getFecha());
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    void guardarPedido_DebeLanzarExcepcion_CuandoFaltanDatosObligatorios() {
        PedidoCreateRequest requestInvalido = new PedidoCreateRequest();

        assertThrows(IllegalArgumentException.class, () -> pedidoService.guardarPedido(requestInvalido));

        verify(pedidoRepository, never()).save(any(Pedido.class));
        verify(productoRepository, never()).save(any(Producto.class));
    }
}
