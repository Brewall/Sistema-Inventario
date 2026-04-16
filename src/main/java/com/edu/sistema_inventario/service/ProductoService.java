package com.edu.sistema_inventario.service;

import com.edu.sistema_inventario.model.Producto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    private final Map<Long, Producto> productos = new ConcurrentHashMap<>();
    private final AtomicLong secuenciaId = new AtomicLong(0);

    public List<Producto> listar() {
        return new ArrayList<>(productos.values());
    }

    public Optional<Producto> obtenerPorId(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return Optional.ofNullable(productos.get(id));
    }

    public Producto crear(Producto producto) {
        validarProducto(producto);
        Long id = secuenciaId.incrementAndGet();
        producto.setId(id);
        productos.put(id, producto);
        return producto;
    }

    public Optional<Producto> actualizar(Long id, Producto productoActualizado) {
        if (id == null || id <= 0 || productoActualizado == null) {
            return Optional.empty();
        }

        validarProducto(productoActualizado);
        Producto existente = productos.get(id);
        if (existente == null) {
            return Optional.empty();
        }

        existente.setNombre(productoActualizado.getNombre());
        existente.setDescripcion(productoActualizado.getDescripcion());
        existente.setPrecio(productoActualizado.getPrecio());
        existente.setStock(productoActualizado.getStock());
        return Optional.of(existente);
    }

    public Optional<Producto> actualizarStock(Long id, Integer stock) {
        if (id == null || id <= 0 || stock == null || stock < 0) {
            return Optional.empty();
        }

        Producto existente = productos.get(id);
        if (existente == null) {
            return Optional.empty();
        }

        existente.setStock(stock);
        return Optional.of(existente);
    }

    public boolean eliminar(Long id) {
        return productos.remove(id) != null;
    }

    public List<Producto> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return List.of();
        }

        String termino = nombre.toLowerCase();
        return productos.values().stream()
                .filter(producto -> producto.getNombre() != null && producto.getNombre().toLowerCase().contains(termino))
                .collect(Collectors.toList());
    }

    public List<Producto> listarStockBajo(Integer umbral) {
        if (umbral == null) {
            return List.of();
        }

        return productos.values().stream()
                .filter(producto -> producto.getStock() != null && producto.getStock() <= umbral)
                .collect(Collectors.toList());
    }

    private void validarProducto(Producto producto) {
        if (producto == null
                || producto.getNombre() == null || producto.getNombre().isBlank()
                || producto.getPrecio() == null || producto.getPrecio() < 0
                || producto.getStock() == null || producto.getStock() < 0) {
            throw new IllegalArgumentException("Datos inválidos");
        }
    }
}
