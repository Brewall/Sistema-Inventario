package com.edu.sistema_inventario.controller;

import com.edu.sistema_inventario.model.Rol;
import com.edu.sistema_inventario.model.Usuario;
import com.edu.sistema_inventario.service.UsuarioService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioService usuarioService;

    @Test
    void registrar_deberiaRetornar201_cuandoDatosValidos() throws Exception {
        Usuario creado = usuario(1L, "Ana", "Perez", "ana@correo.com", "123456");
        when(usuarioService.registrar(any(Usuario.class))).thenReturn(creado);

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creado)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void registrar_deberiaRetornar400_cuandoDatosInvalidos() throws Exception {
        Usuario invalido = usuario(null, "", "Perez", "ana@correo.com", "123456");

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrar_deberiaRetornar404_cuandoRutaNoExiste() throws Exception {
        Usuario usuario = usuario(null, "Ana", "Perez", "ana@correo.com", "123456");

        mockMvc.perform(post("/api/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void listar_deberiaRetornar200_cuandoHayUsuarios() throws Exception {
        when(usuarioService.listar()).thenReturn(List.of(usuario(1L, "Ana", "Perez", "ana@correo.com", "123456")));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Ana"));
    }

    @Test
    void listar_deberiaRetornar400_cuandoLaRutaEsInvalida() throws Exception {
        mockMvc.perform(get("/api/usuarios/%"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listar_deberiaRetornar404_cuandoRutaNoExiste() throws Exception {
        mockMvc.perform(get("/api/usuarios/listado"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buscarPorId_deberiaRetornar200_cuandoExiste() throws Exception {
        when(usuarioService.buscarPorId(1L)).thenReturn(Optional.of(usuario(1L, "Ana", "Perez", "ana@correo.com", "123456")));

        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@correo.com"));
    }

    @Test
    void buscarPorId_deberiaRetornar400_cuandoIdNoEsNumerico() throws Exception {
        mockMvc.perform(get("/api/usuarios/id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buscarPorId_deberiaRetornar404_cuandoNoExiste() throws Exception {
        when(usuarioService.buscarPorId(88L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/usuarios/88"))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminar_deberiaRetornar200_cuandoSeEliminaCorrectamente() throws Exception {
        when(usuarioService.eliminar(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/usuarios/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminar_deberiaRetornar400_cuandoIdNoEsNumerico() throws Exception {
        mockMvc.perform(delete("/api/usuarios/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void eliminar_deberiaRetornar404_cuandoNoExiste() throws Exception {
        when(usuarioService.eliminar(250L)).thenReturn(false);

        mockMvc.perform(delete("/api/usuarios/250"))
                .andExpect(status().isNotFound());
    }

    private Usuario usuario(Long id, String nombre, String apellido, String email, String password) {
        Rol rol = new Rol();
        rol.setId(1L);
        rol.setNombre("ADMIN");
        rol.setDescripcion("Administrador");
        rol.setCodigo("ADM");
        rol.setActivo(true);

        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setEmail(email);
        usuario.setPassword(password);
        usuario.setRol(rol);
        return usuario;
    }
}
