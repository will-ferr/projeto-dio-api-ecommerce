package br.com.dio.ecommerce.dto;

import br.com.dio.ecommerce.model.Categoria;

public record CategoriaResponse(Long id, String nome) {

    public static CategoriaResponse from(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNome());
    }
}
