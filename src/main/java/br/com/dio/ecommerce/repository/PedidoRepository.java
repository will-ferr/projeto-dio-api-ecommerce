package br.com.dio.ecommerce.repository;

import br.com.dio.ecommerce.model.Pedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @EntityGraph(attributePaths = {"cliente", "itens", "itens.produto"})
    List<Pedido> findAllByOrderByIdDesc();

    @EntityGraph(attributePaths = {"cliente", "itens", "itens.produto"})
    Optional<Pedido> findComItensById(Long id);

    boolean existsByClienteId(Long clienteId);

    boolean existsByItensProdutoId(Long produtoId);
}
