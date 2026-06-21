package com.edu.sistema_inventario.service;

import com.edu.sistema_inventario.model.Categoria;
import com.edu.sistema_inventario.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {
    
    private final CategoriaRepository categoriaRepository;
    
    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }
    
    public Categoria guardarCategoria(Categoria categoria) {
        validarCategoria(categoria);
        return categoriaRepository.save(categoria);
    }
    
    public Categoria obtenerCategoriaPorId(Long id) {
        Optional<Categoria> categoria = categoriaRepository.findById(id);
        return categoria.orElse(null);
    }
    
    public List<Categoria> obtenerTodasLasCategorias() {
        return categoriaRepository.findAll();
    }
    
    public Categoria buscarPorNombre(String nombre) {
        return categoriaRepository.findByNombreIgnoreCase(nombre);
    }
    
    public Categoria actualizarCategoria(Long id, Categoria categoriaActualizada) {
        Optional<Categoria> categoriaExistente = categoriaRepository.findById(id);
        if (categoriaExistente.isPresent()) {
            Categoria categoria = categoriaExistente.get();
            if (categoriaActualizada.getNombre() != null) {
                categoria.setNombre(categoriaActualizada.getNombre());
            }
            if (categoriaActualizada.getDescripcion() != null) {
                categoria.setDescripcion(categoriaActualizada.getDescripcion());
            }
            if (categoriaActualizada.getEstado() != null) {
                categoria.setEstado(categoriaActualizada.getEstado());
            }
            return categoriaRepository.save(categoria);
        }
        return null;
    }
    
    public boolean eliminarCategoria(Long id) {
        if (categoriaRepository.existsById(id)) {
            categoriaRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    // Métodos heredados para compatibilidad
    public Categoria crear(Categoria categoria) {
        return guardarCategoria(categoria);
    }
    
    public List<Categoria> listar() {
        return obtenerTodasLasCategorias();
    }
    
    public Optional<Categoria> buscarPorId(Long id) {
        return categoriaRepository.findById(id);
    }
    
    public boolean eliminar(Long id) {
        if (categoriaRepository.existsById(id)) {
            categoriaRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private void validarCategoria(Categoria categoria) {
        if (categoria == null ||
            categoria.getNombre() == null || categoria.getNombre().isBlank() ||
            categoria.getDescripcion() == null || categoria.getDescripcion().isBlank() ||
            categoria.getEstado() == null) {
            throw new IllegalArgumentException("Datos de categoría inválidos");
        }
    }
}
