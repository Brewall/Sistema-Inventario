package com.edu.sistema_inventario.service;

import com.edu.sistema_inventario.model.Producto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductoServiceTest {

    @Test
    void crearYConsultarProducto() {
        ProductoService service = new ProductoService();

        Producto producto = new Producto();
        producto.setNombre("Mouse");
        producto.setDescripcion("Mouse inalámbrico");
        producto.setPrecio(25.5);
        producto.setStock(10);

        Producto creado = service.crear(producto);

        assertNotNull(creado.getId());
        assertEquals("Mouse", creado.getNombre());
        assertTrue(service.obtenerPorId(creado.getId()).isPresent());
        assertEquals("Mouse", service.obtenerPorId(creado.getId()).get().getNombre());
    }
}
