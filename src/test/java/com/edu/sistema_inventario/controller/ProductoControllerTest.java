package com.edu.sistema_inventario.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.edu.sistema_inventario.model.Producto;
import com.edu.sistema_inventario.service.ProductoService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; 

    
    @MockBean
    private ProductoService productoService; 

    private Producto productoPrueba;

    @BeforeEach
    void setUp() {
        
        productoPrueba = new Producto();
        productoPrueba.setId(1L);
        productoPrueba.setNombre("Laptop UTP");
        productoPrueba.setPrecio(2500.0);
        productoPrueba.setStock(15);
    }

    @Test
    public void testListarProductos() throws Exception {
        Mockito.when(productoService.listar()).thenReturn(Arrays.asList(productoPrueba));

        mockMvc.perform(get("/api/productos"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].nombre").value("Laptop UTP"));
    }

    @Test
    public void testObtenerProductoPorId_NoEncontrado() throws Exception {
        Mockito.when(productoService.obtenerPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/productos/99"))
               .andExpect(status().isNotFound()); 
    }

    @Test
    public void testCrearProducto_Exito() throws Exception {
        Mockito.when(productoService.crear(Mockito.any(Producto.class))).thenReturn(productoPrueba);

        mockMvc.perform(post("/api/productos")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(productoPrueba))) 
               .andExpect(status().isCreated()); 
    }

    @Test
    public void testCrearProducto_Invalido() throws Exception {
        
        Producto productoInvalido = new Producto(); 

        mockMvc.perform(post("/api/productos")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(productoInvalido)))
               .andExpect(status().isBadRequest()) 
               .andExpect(content().string("Datos inválidos: nombre, precio y stock son obligatorios"));
    }
}