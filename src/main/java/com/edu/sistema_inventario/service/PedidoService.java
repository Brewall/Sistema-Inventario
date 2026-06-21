package com.edu.sistema_inventario.service;

import com.edu.sistema_inventario.dto.PedidoCreateRequest;
import com.edu.sistema_inventario.dto.PedidoItemRequest;
import com.edu.sistema_inventario.model.Pedido;
import com.edu.sistema_inventario.model.PedidoEstado;
import com.edu.sistema_inventario.model.PedidoItem;
import com.edu.sistema_inventario.model.Producto;
import com.edu.sistema_inventario.repository.PedidoRepository;
import com.edu.sistema_inventario.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
public class PedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoService.class);

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final AuditoriaNegocioService auditoriaNegocioService;

    public PedidoService(
            PedidoRepository pedidoRepository,
            ProductoRepository productoRepository,
            AuditoriaNegocioService auditoriaNegocioService
    ) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.auditoriaNegocioService = auditoriaNegocioService;
    }

    @Transactional
    public Pedido guardarPedido(PedidoCreateRequest request) {
        validarPedido(request);

        Pedido pedido = new Pedido();
        pedido.setUsuarioId(request.getUsuarioId());
        pedido.setEstado(PedidoEstado.PENDIENTE);

        BigDecimal total = BigDecimal.ZERO;

        for (PedidoItemRequest itemRequest : request.getItems()) {
            Producto producto = productoRepository.findById(itemRequest.getProductoId())
                    .orElseThrow(() -> new IllegalArgumentException("El producto no existe"));

            if (Boolean.FALSE.equals(producto.getActivo())) {
                throw new IllegalArgumentException("El producto esta inactivo");
            }

            if (itemRequest.getCantidad() == null || itemRequest.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
            }

            if (producto.getStock() == null || producto.getStock() < itemRequest.getCantidad()) {
                throw new IllegalArgumentException("Stock insuficiente");
            }

            BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(itemRequest.getCantidad()));
            total = total.add(subtotal);

            PedidoItem item = new PedidoItem();
            item.setProductoId(producto.getId());
            item.setCantidad(itemRequest.getCantidad());
            item.setPrecioUnitario(producto.getPrecio());
            item.setSubtotal(subtotal);
            pedido.agregarItem(item);

            producto.setStock(producto.getStock() - itemRequest.getCantidad());
            productoRepository.save(producto);

            if (producto.getStock() < 5) {
                registrarEventoSeguro(
                        "STOCK_BAJO",
                        "El stock del producto '" + producto.getNombre() + "' (ID: " + producto.getId() + ") ha quedado bajo el umbral: " + producto.getStock()
                );
            }
        }

        pedido.setTotal(total);
        Pedido guardado = pedidoRepository.save(pedido);
        registrarEventoSeguro(
                "PEDIDO_CREADO",
                "Pedido creado con ID: " + guardado.getId() + " para el usuario ID: " + guardado.getUsuarioId() + " por un total de " + guardado.getTotal()
        );
        return guardado;
    }

    public Pedido obtenerPedidoPorId(Long id) {
        Optional<Pedido> pedido = pedidoRepository.findById(id);
        return pedido.orElse(null);
    }

    public List<Pedido> obtenerTodosLosPedidos() {
        return pedidoRepository.findAll();
    }

    public Page<Pedido> obtenerTodosLosPedidos(Pageable pageable) {
        return pedidoRepository.findAll(pageable);
    }

    public List<Pedido> obtenerPedidosPorUsuario(Long usuarioId) {
        return pedidoRepository.findByUsuarioId(usuarioId);
    }

    public Page<Pedido> obtenerPedidosPorUsuario(Long usuarioId, Pageable pageable) {
        return pedidoRepository.findByUsuarioId(usuarioId, pageable);
    }

    @Transactional
    public Pedido actualizarEstadoPedido(Long id, String nuevoEstado) {
        Optional<Pedido> pedidoExistente = pedidoRepository.findById(id);
        if (pedidoExistente.isEmpty()) {
            return null;
        }

        Pedido pedido = pedidoExistente.get();
        PedidoEstado estadoActual = pedido.getEstado();
        PedidoEstado estadoNuevo;

        try {
            estadoNuevo = PedidoEstado.valueOf(nuevoEstado.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Estado de pedido invalido");
        }

        if (estadoActual == estadoNuevo) {
            return pedido;
        }

        if (estadoActual == PedidoEstado.ENTREGADO || estadoActual == PedidoEstado.CANCELADO) {
            throw new IllegalArgumentException("No se permite cambiar el estado de un pedido finalizado");
        }

        if (estadoNuevo != PedidoEstado.ENTREGADO && estadoNuevo != PedidoEstado.CANCELADO) {
            throw new IllegalArgumentException("Transicion de estado invalida");
        }

        if (estadoActual == PedidoEstado.PENDIENTE && estadoNuevo == PedidoEstado.CANCELADO) {
            reabastecerStockSiCorresponde(pedido);
        }

        pedido.setEstado(estadoNuevo);
        Pedido guardado = pedidoRepository.save(pedido);
        if (estadoNuevo == PedidoEstado.CANCELADO) {
            registrarEventoSeguro(
                    "PEDIDO_CANCELADO",
                    "Pedido cancelado con ID: " + guardado.getId() + ". Stock reabastecido."
            );
        }
        return guardado;
    }

    public Pedido actualizarTotalPedido(Long id, BigDecimal nuevoTotal) {
        Optional<Pedido> pedidoExistente = pedidoRepository.findById(id);
        if (pedidoExistente.isPresent()) {
            Pedido pedido = pedidoExistente.get();
            pedido.setTotal(nuevoTotal);
            return pedidoRepository.save(pedido);
        }
        return null;
    }

    public boolean eliminarPedido(Long id) {
        if (pedidoRepository.existsById(id)) {
            pedidoRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<Pedido> obtenerPedidosPorEstado(String estado) {
        return obtenerTodosLosPedidos().stream()
                .filter(p -> p.getEstado() != null && estado.equalsIgnoreCase(p.getEstado().name()))
                .toList();
    }

    public Page<Pedido> obtenerPedidosPorEstado(String estado, Pageable pageable) {
        try {
            PedidoEstado pedidoEstado = PedidoEstado.valueOf(estado.toUpperCase());
            return pedidoRepository.findByEstado(pedidoEstado, pageable);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Estado de pedido invalido");
        }
    }

    public Page<Pedido> obtenerPedidosPorFecha(String fromIso, String toIso, Pageable pageable) {
        try {
            LocalDateTime from = LocalDateTime.parse(fromIso);
            LocalDateTime to = LocalDateTime.parse(toIso);
            return pedidoRepository.findByFechaBetween(from, to, pageable);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Formato de fecha invalido, use ISO_LOCAL_DATE_TIME");
        }
    }

    private void validarPedido(PedidoCreateRequest request) {
        if (request == null || request.getUsuarioId() == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Datos de pedido invalidos");
        }

        for (PedidoItemRequest item : request.getItems()) {
            if (item == null || item.getProductoId() == null || item.getCantidad() == null) {
                throw new IllegalArgumentException("Los items del pedido son invalidos");
            }
        }
    }

    private void reabastecerStockSiCorresponde(Pedido pedido) {
        if (pedido.getItems() != null && !pedido.getItems().isEmpty()) {
            for (PedidoItem item : pedido.getItems()) {
                Producto producto = productoRepository.findById(item.getProductoId())
                        .orElseThrow(() -> new IllegalArgumentException("El producto del pedido no existe"));

                producto.setStock(producto.getStock() + item.getCantidad());
                productoRepository.save(producto);
            }
            return;
        }

        if (pedido.getProductoId() == null || pedido.getCantidad() == null) {
            return;
        }

        Producto producto = productoRepository.findById(pedido.getProductoId())
                .orElseThrow(() -> new IllegalArgumentException("El producto del pedido no existe"));

        producto.setStock(producto.getStock() + pedido.getCantidad());
        productoRepository.save(producto);
    }

    private void registrarEventoSeguro(String tipoEvento, String descripcion) {
        try {
            auditoriaNegocioService.registrarEvento(tipoEvento, descripcion);
        } catch (Exception ex) {
            log.warn("No se pudo registrar el evento de auditoria '{}' sin afectar la operacion principal: {}", tipoEvento, ex.getMessage());
            log.debug("Detalle de error al registrar auditoria", ex);
        }
    }
}
