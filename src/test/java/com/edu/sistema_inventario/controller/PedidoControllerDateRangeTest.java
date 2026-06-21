package com.edu.sistema_inventario.controller;

import com.edu.sistema_inventario.model.Pedido;
import com.edu.sistema_inventario.model.PedidoEstado;
import com.edu.sistema_inventario.service.PedidoService;
import com.edu.sistema_inventario.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PedidoController.class)
@AutoConfigureMockMvc(addFilters = false)
class PedidoControllerDateRangeTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PedidoService pedidoService;

    @MockBean
    private UsuarioService usuarioService;

    @Test
    void fechaRango_deberiaRetornarPage() throws Exception {
        Pedido p = new Pedido(5L, BigDecimal.valueOf(50.0));
        p.setEstado(PedidoEstado.ENTREGADO);
        when(pedidoService.obtenerPedidosPorFecha(anyString(), anyString(), any())).thenReturn(new PageImpl<>(List.of(p)));

        mockMvc.perform(get("/api/pedidos?from=2026-06-01T00:00:00&to=2026-06-30T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].usuarioId").value(5));
    }

    @Test
    void fechaRango_formatoInvalido_deberiaRetornar400() throws Exception {
        when(pedidoService.obtenerPedidosPorFecha(anyString(), anyString(), any())).thenThrow(new IllegalArgumentException("Formato de fecha inválido"));

        mockMvc.perform(get("/api/pedidos?from=invalid-date&to=also-invalid"))
            .andExpect(status().isBadRequest());
    }
}
