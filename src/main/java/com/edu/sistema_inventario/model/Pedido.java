package com.edu.sistema_inventario.model;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@SQLDelete(sql = "UPDATE pedidos SET activo = false WHERE id = ? AND version = ?")
@Where(clause = "activo = true")
public class Pedido extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;
    
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "producto_id")
    private Long productoId;

    @Column(name = "cantidad")
    private Integer cantidad;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;
    
    @Column(nullable = false)
    private BigDecimal total;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PedidoEstado estado;

    @Column(nullable = false)
    private Boolean activo = true;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PedidoItem> items;

    public Pedido() {
        this.fecha = LocalDateTime.now();
        this.estado = PedidoEstado.PENDIENTE;
        this.total = BigDecimal.ZERO;
        this.items = new ArrayList<>();
    }

    public Pedido(Long usuarioId, BigDecimal total) {
        this.usuarioId = usuarioId;
        this.total = total;
        this.fecha = LocalDateTime.now();
        this.estado = PedidoEstado.PENDIENTE;
        this.items = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
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

    public List<PedidoItem> getItems() {
        return items;
    }

    public void setItems(List<PedidoItem> items) {
        this.items = items;
    }

    public void agregarItem(PedidoItem item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
        item.setPedido(this);
    }
}
