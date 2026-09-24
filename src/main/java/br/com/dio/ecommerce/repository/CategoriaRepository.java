package br.com.dio.ecommerce.repository;

import br.com.dio.ecommerce.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositórios do Spring Data também são singletons: o Spring cria UM proxy para esta
 * interface e o injeta em todos os services que dependem dela.
 */
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}
