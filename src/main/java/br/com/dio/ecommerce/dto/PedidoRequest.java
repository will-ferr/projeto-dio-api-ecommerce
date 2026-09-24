package br.com.dio.ecommerce.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PedidoRequest(
        @NotNull Long clienteId,
        @NotEmpty List<@Valid @NotNull ItemPedidoRequest> itens
) {
}
