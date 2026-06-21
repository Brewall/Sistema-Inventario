package com.edu.sistema_inventario.controller;

import com.edu.sistema_inventario.model.Categoria;
import com.edu.sistema_inventario.service.CategoriaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoriaController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoriaService categoriaService;

    @Test
    void crear_deberiaRetornar201_cuandoDatosValidos() throws Exception {
        Categoria creada = categoria(1L, "Perifericos", "Dispositivos externos", "ACTIVA");
        when(categoriaService.crear(any(Categoria.class))).thenReturn(creada);

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creada)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void listar_deberiaRetornar200_cuandoHayCategorias() throws Exception {
        when(categoriaService.listar()).thenReturn(List.of(categoria(1L, "Hardware", "Componentes fisicos", "ACTIVA")));

        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Hardware"));
    }

    @Test
    void buscarPorId_deberiaRetornar200_cuandoExiste() throws Exception {
        when(categoriaService.buscarPorId(2L)).thenReturn(Optional.of(categoria(2L, "Software", "Licencias", "ACTIVA")));

        mockMvc.perform(get("/api/categorias/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Software"));
    }

    @Test
    void buscarPorId_deberiaRetornar404_cuandoNoExiste() throws Exception {
        when(categoriaService.buscarPorId(150L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/categorias/150"))
                .andExpect(status().isNotFound());
    }

    private Categoria categoria(Long id, String nombre, String descripcion, String estado) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNombre(nombre);
        categoria.setDescripcion(descripcion);
        categoria.setEstado(estado);
        return categoria;
    }
}
