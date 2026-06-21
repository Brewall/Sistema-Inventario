package com.edu.sistema_inventario.service;

import com.edu.sistema_inventario.model.Categoria;
import com.edu.sistema_inventario.repository.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void guardar_DebeGuardarCategoriaExitosamente() {
        Categoria categoria = new Categoria();
        categoria.setNombre("Electrónica");
        categoria.setDescripcion("Productos electrónicos");

        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);

        Categoria resultado = categoriaService.guardarCategoria(categoria);

        assertNotNull(resultado);
        assertEquals("Electrónica", resultado.getNombre());
        verify(categoriaRepository, times(1)).save(categoria);
    }

    @Test
    void guardar_DebeLanzarExcepcion_CuandoFaltanDatosObligatorios() {
        Categoria categoriaInvalida = new Categoria();
        
        assertThrows(IllegalArgumentException.class, () -> categoriaService.guardarCategoria(categoriaInvalida));
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }
}
