package com.edu.sistema_inventario.controller;

import com.edu.sistema_inventario.model.Producto;
import com.edu.sistema_inventario.service.ProductoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductoService productoService;

    @Test
    void crear_deberiaRetornar201_cuandoDatosValidos() throws Exception {
        Producto creado = producto(1L, "Laptop", BigDecimal.valueOf(1500.0), 10);
        when(productoService.guardarProducto(any(Producto.class))).thenReturn(creado);

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creado)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void listar_deberiaRetornar200_cuandoHayProductos() throws Exception {
        var lista = List.of(producto(1L, "Monitor", BigDecimal.valueOf(200.0), 5));
        when(productoService.obtenerTodosLosProductos(any(Pageable.class))).thenReturn(new PageImpl<>(lista));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombre").value("Monitor"));
    }

    @Test
    void listar_conCategoria_deberiaFiltrarYRetornar200() throws Exception {
        var lista = List.of(producto(1L, "Monitor", BigDecimal.valueOf(200.0), 5));
        when(productoService.obtenerProductosPorCategoria(org.mockito.ArgumentMatchers.eq(1L), any(Pageable.class))).thenReturn(new PageImpl<>(lista));

        mockMvc.perform(get("/api/productos?categoriaId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombre").value("Monitor"));
    }

    @Test
    void buscarPorId_deberiaRetornar200_cuandoExiste() throws Exception {
        when(productoService.obtenerProductoPorId(2L)).thenReturn(producto(2L, "Teclado", BigDecimal.valueOf(50.0), 15));

        mockMvc.perform(get("/api/productos/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Teclado"));
    }

    @Test
    void buscarPorId_deberiaRetornar404_cuandoNoExiste() throws Exception {
        when(productoService.obtenerProductoPorId(150L)).thenReturn(null);

        mockMvc.perform(get("/api/productos/150"))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminar_deberiaRetornar200_cuandoSeEliminaCorrectamente() throws Exception {
        when(productoService.eliminarProducto(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/productos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminar_deberiaRetornar404_cuandoNoExiste() throws Exception {
        when(productoService.eliminarProducto(250L)).thenReturn(false);

        mockMvc.perform(delete("/api/productos/250"))
                .andExpect(status().isNotFound());
    }

    private Producto producto(Long id, String nombre, BigDecimal precio, Integer stock) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre(nombre);
        producto.setPrecio(precio);
        producto.setStock(stock);
        producto.setCategoriaId(1L);
        return producto;
    }
}
