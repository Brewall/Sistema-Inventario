package com.edu.sistema_inventario.service;

import com.edu.sistema_inventario.model.Producto;
import com.edu.sistema_inventario.model.Categoria;
import com.edu.sistema_inventario.repository.ProductoRepository;
import com.edu.sistema_inventario.repository.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ProductoService productoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void guardar_DebeGuardarProductoExitosamente() {
        // Arrange
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Laptops");

         Producto producto = new Producto();
        producto.setNombre("Laptop X");
        producto.setPrecio(java.math.BigDecimal.valueOf(1500.0));
        producto.setStock(10);
        producto.setCategoriaId(1L);

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        // Act
        Producto resultado = productoService.guardarProducto(producto);

        // Assert
        assertNotNull(resultado);
        assertEquals("Laptop X", resultado.getNombre());
        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    void guardar_DebeLanzarExcepcion_CuandoFaltanDatosObligatorios() {
        // Arrange
        Producto productoInvalido = new Producto(); // Faltan datos requeridos

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            productoService.guardarProducto(productoInvalido);
        });

        verify(productoRepository, never()).save(any(Producto.class));
    }
}
