package br.com.dio.ecommerce.dto;

import br.com.dio.ecommerce.model.Cliente;

public record ClienteResponse(Long id, String nome, String email, String cpf) {

    public static ClienteResponse from(Cliente cliente) {
        return new ClienteResponse(cliente.getId(), cliente.getNome(), cliente.getEmail(), cliente.getCpf());
    }
}
