package com.edu.sistema_inventario.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PedidoCreateRequest {
    @NotNull(message = "El usuario es obligatorio")
    private Long usuarioId;

    @Valid
    @Size(min = 1, message = "Debe enviar al menos un item")
    private List<PedidoItemRequest> items = new ArrayList<>();

    public PedidoCreateRequest() {
    }

    public PedidoCreateRequest(Long usuarioId, List<PedidoItemRequest> items) {
        this.usuarioId = usuarioId;
        this.items = items;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public List<PedidoItemRequest> getItems() {
        return items;
    }

    public void setItems(List<PedidoItemRequest> items) {
        this.items = items;
    }
}