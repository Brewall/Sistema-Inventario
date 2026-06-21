package com.edu.sistema_inventario.controller;

import com.edu.sistema_inventario.dto.PedidoResponse;
import com.edu.sistema_inventario.model.Pedido;
import com.edu.sistema_inventario.model.PedidoEstado;
import com.edu.sistema_inventario.model.PedidoItem;
import com.edu.sistema_inventario.model.Producto;
import com.edu.sistema_inventario.model.Categoria;
import com.edu.sistema_inventario.model.Usuario;
import com.edu.sistema_inventario.service.CategoriaService;
import com.edu.sistema_inventario.service.PedidoService;
import com.edu.sistema_inventario.service.ProductoService;
import com.edu.sistema_inventario.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PedidoController.class)
@AutoConfigureMockMvc(addFilters = false)
class PedidoControllerPaginationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PedidoService pedidoService;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private ProductoService productoService;

    @MockBean
    private CategoriaService categoriaService;

    @Test
    void listar_deberiaRetornarPage() throws Exception {
        Pedido p = new Pedido(1L, BigDecimal.valueOf(10.0));
        p.setFecha(null);
        when(pedidoService.obtenerTodosLosPedidos(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p)));

        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].usuarioId").value(1));
    }

    @Test
    void misPedidos_deberiaRetornarPage() throws Exception {
        Pedido p = new Pedido(2L, BigDecimal.valueOf(20.0));
        p.setFecha(null);
        when(pedidoService.obtenerPedidosPorUsuario(anyLong(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p)));

        mockMvc.perform(get("/api/pedidos/mis-pedidos/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].usuarioId").value(2));
    }

    @Test
    void listar_porEstado_deberiaRetornarPage() throws Exception {
        Pedido p = new Pedido(3L, BigDecimal.valueOf(30.0));
        p.setFecha(null);
        p.setEstado(PedidoEstado.PENDIENTE);
        when(pedidoService.obtenerPedidosPorEstado(anyString(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p)));

        mockMvc.perform(get("/api/pedidos?estado=PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].estado").value("PENDIENTE"));
    }

    @Test
    void obtenerMisPedidos_otroUsuario_deberiaRetornar403() throws Exception {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        
        Usuario mockUser = new Usuario();
        mockUser.setId(1L);
        mockUser.setEmail("test@email.com");
        
        when(usuarioService.findByEmail("test@email.com")).thenReturn(mockUser);
        
        org.springframework.security.core.Authentication auth = 
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "test@email.com", null, List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
            );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
        
        try {
            mockMvc.perform(get("/api/pedidos/mis-pedidos/2"))
                    .andExpect(status().isForbidden());
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    void crearPedido_otroUsuario_deberiaRetornar403() throws Exception {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        
        Usuario mockUser = new Usuario();
        mockUser.setId(1L);
        mockUser.setEmail("test@email.com");
        
        when(usuarioService.findByEmail("test@email.com")).thenReturn(mockUser);
        
        org.springframework.security.core.Authentication auth = 
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "test@email.com", null, List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
            );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
        
        String requestJson = "{\"usuarioId\": 2, \"items\": [{\"productoId\": 1, \"cantidad\": 5}]}";
        
        try {
            mockMvc.perform(post("/api/pedidos")
                            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isForbidden());
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    void obtenerMisPedidos_mismoUsuario_deberiaRetornarPage() throws Exception {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        
        Usuario mockUser = new Usuario();
        mockUser.setId(1L);
        mockUser.setEmail("test@email.com");
        
        when(usuarioService.findByEmail("test@email.com")).thenReturn(mockUser);
        
        org.springframework.security.core.Authentication auth = 
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "test@email.com", null, List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
            );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
        
        Pedido p = new Pedido(1L, BigDecimal.valueOf(20.0));
        p.setFecha(null);
        when(pedidoService.obtenerPedidosPorUsuario(anyLong(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p)));

        try {
            mockMvc.perform(get("/api/pedidos/mis-pedidos/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].usuarioId").value(1));
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    void obtenerMisPedidos_admin_deberiaRetornarPage() throws Exception {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        
        org.springframework.security.core.Authentication auth = 
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "admin@email.com", null, List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))
            );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
        
        Pedido p = new Pedido(2L, BigDecimal.valueOf(20.0));
        p.setFecha(null);
        when(pedidoService.obtenerPedidosPorUsuario(anyLong(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p)));

        try {
            mockMvc.perform(get("/api/pedidos/mis-pedidos/2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].usuarioId").value(2));
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    void buscarPorId_otroUsuario_deberiaRetornar403() throws Exception {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();

        Usuario mockUser = new Usuario();
        mockUser.setId(1L);
        mockUser.setEmail("test@email.com");

        when(usuarioService.findByEmail("test@email.com")).thenReturn(mockUser);

        Pedido pedido = new Pedido(2L, BigDecimal.valueOf(25.0));
        pedido.setId(50L);
        pedido.setFecha(null);
        when(pedidoService.obtenerPedidoPorId(50L)).thenReturn(pedido);

        org.springframework.security.core.Authentication auth =
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "test@email.com", null, List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
            );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            mockMvc.perform(get("/api/pedidos/50"))
                    .andExpect(status().isForbidden());
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    void buscarPorId_mismoUsuario_deberiaRetornar200() throws Exception {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();

        Usuario mockUser = new Usuario();
        mockUser.setId(1L);
        mockUser.setEmail("test@email.com");

        when(usuarioService.findByEmail("test@email.com")).thenReturn(mockUser);

        Pedido pedido = new Pedido(1L, BigDecimal.valueOf(25.0));
        pedido.setId(51L);
        pedido.setFecha(null);
        when(pedidoService.obtenerPedidoPorId(51L)).thenReturn(pedido);

        org.springframework.security.core.Authentication auth =
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "test@email.com", null, List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
            );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            mockMvc.perform(get("/api/pedidos/51"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.usuarioId").value(1));
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    void buscarPorId_admin_deberiaRetornar200() throws Exception {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();

        Pedido pedido = new Pedido(2L, BigDecimal.valueOf(25.0));
        pedido.setId(52L);
        pedido.setFecha(null);
        when(pedidoService.obtenerPedidoPorId(52L)).thenReturn(pedido);

        org.springframework.security.core.Authentication auth =
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "admin@email.com", null, List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))
            );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            mockMvc.perform(get("/api/pedidos/52"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.usuarioId").value(2));
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    void buscarPorId_deberiaRetornarItemsEnriquecidos() throws Exception {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();

        Pedido pedido = new Pedido(2L, BigDecimal.valueOf(25.0));
        pedido.setId(53L);
        pedido.setFecha(null);

        PedidoItem item = new PedidoItem();
        item.setProductoId(10L);
        item.setCantidad(2);
        item.setPrecioUnitario(BigDecimal.valueOf(15.50));
        item.setSubtotal(BigDecimal.valueOf(31.00));
        pedido.agregarItem(item);

        Producto producto = new Producto();
        producto.setId(10L);
        producto.setNombre("Mouse Gamer");
        producto.setCategoriaId(3L);

        Categoria categoria = new Categoria();
        categoria.setId(3L);
        categoria.setNombre("Perifericos");

        when(pedidoService.obtenerPedidoPorId(53L)).thenReturn(pedido);
        when(productoService.obtenerProductoPorId(10L)).thenReturn(producto);
        when(categoriaService.obtenerCategoriaPorId(3L)).thenReturn(categoria);

        org.springframework.security.core.Authentication auth =
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "admin@email.com", null, List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))
            );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            mockMvc.perform(get("/api/pedidos/53"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items[0].productoId").value(10))
                    .andExpect(jsonPath("$.items[0].productoNombre").value("Mouse Gamer"))
                    .andExpect(jsonPath("$.items[0].categoriaId").value(3))
                    .andExpect(jsonPath("$.items[0].categoriaNombre").value("Perifericos"))
                    .andExpect(jsonPath("$.items[0].precioUnitario").value(15.5))
                    .andExpect(jsonPath("$.items[0].subtotal").value(31.0));
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }
}
