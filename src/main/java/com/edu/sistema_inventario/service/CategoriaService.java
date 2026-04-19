package com.edu.sistema_inventario.service;

import com.edu.sistema_inventario.model.Categoria;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CategoriaService {

    private final Map<Long, Categoria> categorias = new ConcurrentHashMap<>();
    private final AtomicLong secuenciaId = new AtomicLong(0);

    public Categoria crear(Categoria categoria) {
        validarCategoria(categoria);
        Long id = secuenciaId.incrementAndGet();
        categoria.setIdCategoria(id);
        categorias.put(id, categoria);
        return categoria;
    }

    public List<Categoria> listar() {
        return new ArrayList<>(categorias.values());
    }

    public Optional<Categoria> buscarPorId(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return Optional.ofNullable(categorias.get(id));
    }

    private void validarCategoria(Categoria categoria) {
        if (categoria == null
                || categoria.getNombreCategoria() == null || categoria.getNombreCategoria().isBlank()
                || categoria.getDescripcionCategoria() == null || categoria.getDescripcionCategoria().isBlank()
                || categoria.getEstado() == null) {
            throw new IllegalArgumentException("Datos inválidos");
        }
    }
}
