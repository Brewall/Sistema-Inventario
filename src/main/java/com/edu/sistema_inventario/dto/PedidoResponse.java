package com.edu.sistema_inventario.dto;

import com.edu.sistema_inventario.model.PedidoEstado;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PedidoResponse {
    private Long id;
    private Long usuarioId;
    private LocalDateTime fecha;
    private BigDecimal total;
    private PedidoEstado estado;
    private List<PedidoItemResponse> items = new ArrayList<>();

    public PedidoResponse() {
    }

    public PedidoResponse(Long id, Long usuarioId, LocalDateTime fecha, BigDecimal total, PedidoEstado estado, List<PedidoItemResponse> items) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.fecha = fecha;
        this.total = total;
        this.estado = estado;
        this.items = items;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public PedidoEstado getEstado() {
        return estado;
    }

    public void setEstado(PedidoEstado estado) {
        this.estado = estado;
    }

    public List<PedidoItemResponse> getItems() {
        return items;
    }

    public void setItems(List<PedidoItemResponse> items) {
        this.items = items;
    }
}