package br.com.dio.ecommerce.dto;

import br.com.dio.ecommerce.model.ItemPedido;

import java.math.BigDecimal;

public record ItemPedidoResponse(
        Long id,
        Long produtoId,
        String produtoNome,
        Integer quantidade,
        BigDecimal precoUnitario,
        BigDecimal subtotal
) {

    public static ItemPedidoResponse from(ItemPedido item) {
        return new ItemPedidoResponse(
                item.getId(),
                item.getProduto().getId(),
                item.getProduto().getNome(),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                item.getSubtotal());
    }
}
