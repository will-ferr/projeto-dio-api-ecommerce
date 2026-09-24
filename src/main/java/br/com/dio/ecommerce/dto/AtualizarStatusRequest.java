package br.com.dio.ecommerce.dto;

import br.com.dio.ecommerce.model.StatusPedido;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusRequest(@NotNull StatusPedido status) {
}
