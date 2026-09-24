package br.com.dio.ecommerce.dto;

import br.com.dio.ecommerce.model.Produto;

import java.math.BigDecimal;

public record ProdutoResponse(
        Long id,
        String nome,
        String descricao,
        BigDecimal preco,
        Integer estoque,
        CategoriaResponse categoria
) {

    public static ProdutoResponse from(Produto produto) {
        return new ProdutoResponse(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getEstoque(),
                CategoriaResponse.from(produto.getCategoria()));
    }
}
