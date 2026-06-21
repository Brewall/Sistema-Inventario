package com.edu.sistema_inventario.service;

import com.edu.sistema_inventario.model.Producto;
import com.edu.sistema_inventario.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {
    
    private final ProductoRepository productoRepository;
    
    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }
    
    public Producto guardarProducto(Producto producto) {
        validarProducto(producto);
        return productoRepository.save(producto);
    }
    
    public Producto obtenerProductoPorId(Long id) {
        Optional<Producto> producto = productoRepository.findById(id);
        return producto.orElse(null);
    }
    
    public List<Producto> obtenerTodosLosProductos() {
        return productoRepository.findAll();
    }
    
    public Page<Producto> obtenerTodosLosProductos(Pageable pageable) {
        return productoRepository.findAll(pageable);
    }
    
    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    public Page<Producto> buscarPorNombre(String nombre, Pageable pageable) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre, pageable);
    }
    
    public List<Producto> obtenerProductosPorCategoria(Long categoriaId) {
        return productoRepository.findByCategoriaId(categoriaId);
    }

    public Page<Producto> obtenerProductosPorCategoria(Long categoriaId, Pageable pageable) {
        return productoRepository.findByCategoriaId(categoriaId, pageable);
    }
    
    public Producto actualizarProducto(Long id, Producto productoActualizado) {
        Optional<Producto> productoExistente = productoRepository.findById(id);
        if (productoExistente.isPresent()) {
            Producto producto = productoExistente.get();
            if (productoActualizado.getNombre() != null) {
                producto.setNombre(productoActualizado.getNombre());
            }
            if (productoActualizado.getDescripcion() != null) {
                producto.setDescripcion(productoActualizado.getDescripcion());
            }
            if (productoActualizado.getPrecio() != null) {
                producto.setPrecio(productoActualizado.getPrecio());
            }
            if (productoActualizado.getStock() != null) {
                producto.setStock(productoActualizado.getStock());
            }
            if (productoActualizado.getCategoriaId() != null) {
                producto.setCategoriaId(productoActualizado.getCategoriaId());
            }
            return productoRepository.save(producto);
        }
        return null;
    }
    
    public boolean eliminarProducto(Long id) {
        if (productoRepository.existsById(id)) {
            productoRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    public boolean incrementarStock(Long id, Integer cantidad) {
        Optional<Producto> productoOpt = productoRepository.findById(id);
        if (productoOpt.isPresent()) {
            Producto producto = productoOpt.get();
            producto.setStock(producto.getStock() + cantidad);
            productoRepository.save(producto);
            return true;
        }
        return false;
    }
    
    public boolean decrementarStock(Long id, Integer cantidad) {
        Optional<Producto> productoOpt = productoRepository.findById(id);
        if (productoOpt.isPresent()) {
            Producto producto = productoOpt.get();
            if (producto.getStock() >= cantidad) {
                producto.setStock(producto.getStock() - cantidad);
                productoRepository.save(producto);
                return true;
            }
        }
        return false;
    }
    
    public boolean tieneStockDisponible(Long id, Integer cantidad) {
        Optional<Producto> productoOpt = productoRepository.findById(id);
        if (productoOpt.isPresent()) {
            return productoOpt.get().getStock() >= cantidad;
        }
        return false;
    }
    
    // Métodos heredados para compatibilidad
    public List<Producto> listar() {
        return obtenerTodosLosProductos();
    }
    
    public Optional<Producto> obtenerPorId(Long id) {
        return productoRepository.findById(id);
    }
    
    public Producto crear(Producto producto) {
        return guardarProducto(producto);
    }
    
    public Optional<Producto> actualizar(Long id, Producto productoActualizado) {
        return Optional.ofNullable(actualizarProducto(id, productoActualizado));
    }
    
    public Optional<Producto> actualizarStock(Long id, Integer stock) {
        if (incrementarStock(id, stock)) {
            return productoRepository.findById(id);
        }
        return Optional.empty();
    }
    
    public boolean eliminar(Long id) {
        return eliminarProducto(id);
    }
    
    public List<Producto> listarStockBajo(Integer umbral) {
        return obtenerTodosLosProductos().stream()
                .filter(p -> p.getStock() != null && p.getStock() <= umbral)
                .toList();
    }
    
    private void validarProducto(Producto producto) {
        if (producto == null || 
            producto.getNombre() == null || producto.getNombre().isBlank() ||
            producto.getPrecio() == null ||
            producto.getStock() == null ||
            producto.getCategoriaId() == null) {
            throw new IllegalArgumentException("Datos de producto inválidos");
        }
    }
}
