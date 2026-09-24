package br.com.dio.ecommerce.repository;

import br.com.dio.ecommerce.model.Produto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    /** Carrega a categoria junto (evita o problema N+1 na listagem). */
    @Override
    @EntityGraph(attributePaths = "categoria")
    List<Produto> findAll();

    boolean existsByCategoriaId(Long categoriaId);
}
