package com.edu.sistema_inventario.controller;

import com.edu.sistema_inventario.dto.PedidoCreateRequest;
import com.edu.sistema_inventario.dto.PedidoItemResponse;
import com.edu.sistema_inventario.dto.PageResponse;
import com.edu.sistema_inventario.dto.PedidoResponse;
import com.edu.sistema_inventario.exception.ApiException;
import com.edu.sistema_inventario.model.Categoria;
import com.edu.sistema_inventario.model.Pedido;
import com.edu.sistema_inventario.model.PedidoItem;
import com.edu.sistema_inventario.model.Producto;
import com.edu.sistema_inventario.service.PedidoService;
import com.edu.sistema_inventario.service.CategoriaService;
import com.edu.sistema_inventario.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;


@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Endpoints para gestión de pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final com.edu.sistema_inventario.service.UsuarioService usuarioService;
    private final ProductoService productoService;
    private final CategoriaService categoriaService;

    public PedidoController(
            PedidoService pedidoService,
            com.edu.sistema_inventario.service.UsuarioService usuarioService,
            ProductoService productoService,
            CategoriaService categoriaService
    ) {
        this.pedidoService = pedidoService;
        this.usuarioService = usuarioService;
        this.productoService = productoService;
        this.categoriaService = categoriaService;
    }

    // Listar todos los pedidos (Solo ADMIN)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Listar todos los pedidos",
            description = "Obtiene la lista de todos los pedidos del sistema. Solo administradores pueden acceder.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de pedidos obtenida"),
                    @ApiResponse(responseCode = "403", description = "No tiene permisos ADMIN")
            }
    )
            @io.swagger.v3.oas.annotations.Parameters({
                    @Parameter(name = "page", in = ParameterIn.QUERY, description = "Page index (0..N)"),
                    @Parameter(name = "size", in = ParameterIn.QUERY, description = "Page size"),
                    @Parameter(name = "sort", in = ParameterIn.QUERY, description = "Sort criteria e.g. field,asc")
            })
            public ResponseEntity<PageResponse<PedidoResponse>> listar(
                    @RequestParam(value = "estado", required = false) @Parameter(description = "Filtrar por estado del pedido, e.g. PENDIENTE, ENTREGADO") String estado,
                    @RequestParam(value = "from", required = false) @Parameter(description = "Fecha desde (ISO_LOCAL_DATE_TIME)", example = "2026-06-01T00:00:00") String from,
                    @RequestParam(value = "to", required = false) @Parameter(description = "Fecha hasta (ISO_LOCAL_DATE_TIME)", example = "2026-06-30T23:59:59") String to,
                    Pageable pageable) {
                Page<Pedido> page;
                if (estado != null && !estado.isBlank()) {
                        page = pedidoService.obtenerPedidosPorEstado(estado, pageable);
                } else if (from != null && to != null) {
                        page = pedidoService.obtenerPedidosPorFecha(from, to, pageable);
                } else {
                        page = pedidoService.obtenerTodosLosPedidos(pageable);
                }
                return ResponseEntity.ok(PageResponse.from(page.map(this::toResponse)));
        }

    // Listar mis pedidos (Cualquier usuario autenticado)
    @GetMapping("/mis-pedidos/{usuarioId}")
    @Operation(
            summary = "Obtener mis pedidos",
            description = "Obtiene los pedidos asociados a un usuario específico. Requiere autenticación.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de pedidos del usuario"),
                    @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
            }
    )
        public ResponseEntity<PageResponse<PedidoResponse>> obtenerMisPedidos(@PathVariable("usuarioId") Long usuarioId, Pageable pageable) {
                validarPropiedadRecurso(usuarioId);
                return ResponseEntity.ok(PageResponse.from(pedidoService.obtenerPedidosPorUsuario(usuarioId, pageable).map(this::toResponse)));
        }

    // Buscar pedido por ID
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener pedido por ID",
            description = "Obtiene los detalles de un pedido específico",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
                    @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
            }
    )
    public ResponseEntity<PedidoResponse> buscarPorId(@PathVariable("id") Long id) {
        Pedido pedido = pedidoService.obtenerPedidoPorId(id);
        if (pedido != null) {
            validarPropiedadRecurso(pedido.getUsuarioId());
            return ResponseEntity.ok(toResponse(pedido));
        }
        throw new ApiException(HttpStatus.NOT_FOUND, "Pedido no encontrado");
    }

    // Crear pedido
    @PostMapping
    @Operation(
            summary = "Crear nuevo pedido",
            description = "Crea un nuevo pedido en el sistema. Requiere autenticación.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Pedido creado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
            }
    )
    public ResponseEntity<PedidoResponse> crearPedido(@Valid @RequestBody PedidoCreateRequest request) {
        validarPropiedadRecurso(request.getUsuarioId());
        Pedido creado = pedidoService.guardarPedido(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(creado));
    }

    // Actualizar estado pedido (Solo ADMIN)
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Actualizar estado del pedido",
            description = "Actualiza el estado de un pedido existente. Solo administradores pueden realizar esta acción.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "403", description = "No tiene permisos ADMIN"),
                    @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
            }
    )
    public ResponseEntity<PedidoResponse> actualizarEstado(@PathVariable("id") Long id, @RequestParam("estado") String estado) {
        Pedido actualizado = pedidoService.actualizarEstadoPedido(id, estado);
        if (actualizado != null) {
            return ResponseEntity.ok(toResponse(actualizado));
        }
        throw new ApiException(HttpStatus.NOT_FOUND, "Pedido no encontrado");
    }

    private PedidoResponse toResponse(Pedido pedido) {
        List<PedidoItemResponse> items = pedido.getItems() == null ? List.of() : pedido.getItems().stream().map(this::toResponse).collect(Collectors.toList());
        return new PedidoResponse(
                pedido.getId(),
                pedido.getUsuarioId(),
                pedido.getFecha(),
                pedido.getTotal(),
                pedido.getEstado(),
                items
        );
    }

    private PedidoItemResponse toResponse(PedidoItem item) {
        Producto producto = productoService.obtenerProductoPorId(item.getProductoId());
        Categoria categoria = null;
        if (producto != null && producto.getCategoriaId() != null) {
            categoria = categoriaService.obtenerCategoriaPorId(producto.getCategoriaId());
        }

        return new PedidoItemResponse(
                item.getProductoId(),
                producto != null ? producto.getNombre() : null,
                producto != null ? producto.getCategoriaId() : null,
                categoria != null ? categoria.getNombre() : null,
                item.getCantidad(),
                item.getPrecioUnitario(),
                item.getSubtotal()
        );
    }

    private void validarPropiedadRecurso(Long usuarioId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            String currentEmail = auth.getName();
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) {
                com.edu.sistema_inventario.model.Usuario usuario = usuarioService.findByEmail(currentEmail);
                if (usuario == null || !usuario.getId().equals(usuarioId)) {
                    throw new ApiException(HttpStatus.FORBIDDEN, "No tiene permisos para acceder a este recurso.");
                }
            }
        }
    }
}
