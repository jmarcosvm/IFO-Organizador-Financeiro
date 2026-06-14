package com.ifo.backend.repository;

import com.ifo.backend.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    // Só o básico, Spring já cria findAll() automaticamente
}