package com.edu.sistema_inventario.controller;

import com.edu.sistema_inventario.model.Categoria;
import com.edu.sistema_inventario.service.CategoriaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoriaService categoriaService;

    @Test
    void crear_deberiaRetornar201_cuandoDatosValidos() throws Exception {
        Categoria creada = categoria(1L, "Perifericos", "Dispositivos externos", true);
        when(categoriaService.crear(any(Categoria.class))).thenReturn(creada);

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creada)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCategoria").value(1L));
    }

    @Test
    void crear_deberiaRetornar400_cuandoDatosInvalidos() throws Exception {
        Categoria invalida = categoria(null, "", "Dispositivos externos", true);

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalida)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crear_deberiaRetornar404_cuandoRutaNoExiste() throws Exception {
        Categoria categoria = categoria(null, "Perifericos", "Dispositivos externos", true);

        mockMvc.perform(post("/api/categorias/nueva")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoria)))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void listar_deberiaRetornar200_cuandoHayCategorias() throws Exception {
        when(categoriaService.listar()).thenReturn(List.of(categoria(1L, "Hardware", "Componentes físicos", true)));

        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreCategoria").value("Hardware"));
    }

    @Test
    void listar_deberiaRetornar400_cuandoLaRutaEsInvalida() throws Exception {
        mockMvc.perform(get("/api/categorias/%"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listar_deberiaRetornar404_cuandoRutaNoExiste() throws Exception {
        mockMvc.perform(get("/api/categorias/listado"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buscarPorId_deberiaRetornar200_cuandoExiste() throws Exception {
        when(categoriaService.buscarPorId(2L)).thenReturn(Optional.of(categoria(2L, "Software", "Licencias", true)));

        mockMvc.perform(get("/api/categorias/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreCategoria").value("Software"));
    }

    @Test
    void buscarPorId_deberiaRetornar400_cuandoIdNoEsNumerico() throws Exception {
        mockMvc.perform(get("/api/categorias/id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buscarPorId_deberiaRetornar404_cuandoNoExiste() throws Exception {
        when(categoriaService.buscarPorId(150L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/categorias/150"))
                .andExpect(status().isNotFound());
    }

    private Categoria categoria(Long id, String nombre, String descripcion, Boolean estado) {
        Categoria categoria = new Categoria();
        categoria.setIdCategoria(id);
        categoria.setNombreCategoria(nombre);
        categoria.setDescripcionCategoria(descripcion);
        categoria.setEstado(estado);
        return categoria;
    }
}
