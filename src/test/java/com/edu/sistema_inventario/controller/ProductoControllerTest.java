package com.edu.sistema_inventario.controller;

import com.edu.sistema_inventario.model.Producto;
import com.edu.sistema_inventario.service.ProductoService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductoController.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductoService productoService;

    @Test
    void listarProductos_deberiaRetornar200_cuandoExistenDatos() throws Exception {
        when(productoService.listar()).thenReturn(List.of(producto(1L, "Teclado", 50.0, 5)));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Teclado"));
    }

    @Test
    void listarProductos_deberiaRetornar400_cuandoLaRutaEsInvalida() throws Exception {
        mockMvc.perform(get("/api/productos/%"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarProductos_deberiaRetornar404_cuandoLaRutaNoExiste() throws Exception {
        mockMvc.perform(get("/api/productos/listado"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerProductoPorId_deberiaRetornar200_cuandoExiste() throws Exception {
        when(productoService.obtenerPorId(1L)).thenReturn(Optional.of(producto(1L, "Mouse", 35.0, 9)));

        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void obtenerProductoPorId_deberiaRetornar400_cuandoIdNoEsNumerico() throws Exception {
        mockMvc.perform(get("/api/productos/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerProductoPorId_deberiaRetornar404_cuandoNoExiste() throws Exception {
        when(productoService.obtenerPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/productos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearProducto_deberiaRetornar201_cuandoDatosValidos() throws Exception {
        Producto entrada = producto(null, "Monitor", 700.0, 4);
        Producto creado = producto(2L, "Monitor", 700.0, 4);
        when(productoService.crear(any(Producto.class))).thenReturn(creado);

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L));
    }

    @Test
    void crearProducto_deberiaRetornar400_cuandoDatosInvalidos() throws Exception {
        Producto entrada = producto(null, "", 700.0, 4);

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearProducto_deberiaRetornar404_cuandoRutaNoExiste() throws Exception {
        Producto entrada = producto(null, "Monitor", 700.0, 4);

        mockMvc.perform(post("/api/productos/no-existe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void actualizarProducto_deberiaRetornar200_cuandoExiste() throws Exception {
        Producto actualizado = producto(1L, "Laptop", 3000.0, 2);
        when(productoService.actualizar(anyLong(), any(Producto.class))).thenReturn(Optional.of(actualizado));

        mockMvc.perform(put("/api/productos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Laptop"));
    }

    @Test
    void actualizarProducto_deberiaRetornar400_cuandoDatosInvalidos() throws Exception {
        Producto invalido = producto(1L, "Laptop", -1.0, 2);

        mockMvc.perform(put("/api/productos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarProducto_deberiaRetornar404_cuandoNoExiste() throws Exception {
        Producto actualizado = producto(100L, "Laptop", 3000.0, 2);
        when(productoService.actualizar(anyLong(), any(Producto.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/productos/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarStock_deberiaRetornar200_cuandoExiste() throws Exception {
        Producto actualizado = producto(1L, "Parlante", 120.0, 30);
        when(productoService.actualizarStock(1L, 30)).thenReturn(Optional.of(actualizado));

        mockMvc.perform(patch("/api/productos/1/stock").param("stock", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(30));
    }

    @Test
    void actualizarStock_deberiaRetornar400_cuandoStockEsInvalido() throws Exception {
        mockMvc.perform(patch("/api/productos/1/stock").param("stock", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarStock_deberiaRetornar404_cuandoProductoNoExiste() throws Exception {
        when(productoService.actualizarStock(500L, 8)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/productos/500/stock").param("stock", "8"))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarProducto_deberiaRetornar200_cuandoSeEliminaCorrectamente() throws Exception {
        when(productoService.eliminar(3L)).thenReturn(true);

        mockMvc.perform(delete("/api/productos/3"))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminarProducto_deberiaRetornar400_cuandoIdNoEsNumerico() throws Exception {
        mockMvc.perform(delete("/api/productos/valor"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void eliminarProducto_deberiaRetornar404_cuandoNoExiste() throws Exception {
        when(productoService.eliminar(77L)).thenReturn(false);

        mockMvc.perform(delete("/api/productos/77"))
                .andExpect(status().isNotFound());
    }

    @Test
    void buscarPorNombre_deberiaRetornar200_cuandoParametroValido() throws Exception {
        when(productoService.buscarPorNombre("lap")).thenReturn(List.of(producto(1L, "Laptop", 1000.0, 3)));

        mockMvc.perform(get("/api/productos/buscar").param("nombre", "lap"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Laptop"));
    }

    @Test
    void buscarPorNombre_deberiaRetornar400_cuandoNombreFalta() throws Exception {
        mockMvc.perform(get("/api/productos/buscar"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buscarPorNombre_deberiaRetornar404_cuandoRutaNoExiste() throws Exception {
        mockMvc.perform(get("/api/productos/buscar-por-nombre").param("nombre", "lap"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarStockBajo_deberiaRetornar200_cuandoUmbralValido() throws Exception {
        when(productoService.listarStockBajo(2)).thenReturn(List.of(producto(4L, "Cable", 15.0, 2)));

        mockMvc.perform(get("/api/productos/stock-bajo").param("umbral", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stock").value(2));
    }

    @Test
    void listarStockBajo_deberiaRetornar400_cuandoUmbralEsInvalido() throws Exception {
        mockMvc.perform(get("/api/productos/stock-bajo").param("umbral", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarStockBajo_deberiaRetornar404_cuandoRutaNoExiste() throws Exception {
        mockMvc.perform(get("/api/productos/stock-minimo").param("umbral", "2"))
                .andExpect(status().isBadRequest());
    }

    private Producto producto(Long id, String nombre, Double precio, Integer stock) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre(nombre);
        producto.setDescripcion("descripcion");
        producto.setPrecio(precio);
        producto.setStock(stock);
        return producto;
    }
}
