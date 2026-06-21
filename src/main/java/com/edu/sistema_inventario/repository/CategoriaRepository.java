package com.edu.sistema_inventario.repository;

import com.edu.sistema_inventario.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    Categoria findByNombreIgnoreCase(String nombre);
}
