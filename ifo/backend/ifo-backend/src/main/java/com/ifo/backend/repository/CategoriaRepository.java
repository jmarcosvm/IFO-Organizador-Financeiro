package com.ifo.backend.repository;

import com.ifo.backend.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Short> {
    // A migration insere as categorias já agrupadas (receitas, necessidades, lazer, investimentos)
    List<Categoria> findAllByOrderByIdCategoriaAsc();
}
