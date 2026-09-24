package br.com.dio.ecommerce.dto;

import br.com.dio.ecommerce.model.Pedido;
import br.com.dio.ecommerce.model.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
        Long id,
        String numero,
        ClienteResponse cliente,
        List<ItemPedidoResponse> itens,
        BigDecimal subtotal,
        BigDecimal valorFrete,
        BigDecimal valorTotal,
        StatusPedido status,
        LocalDateTime dataCriacao
) {

    public static PedidoResponse from(Pedido pedido) {
        return new PedidoResponse(
                pedido.getId(),
                pedido.getNumero(),
                ClienteResponse.from(pedido.getCliente()),
                pedido.getItens().stream().map(ItemPedidoResponse::from).toList(),
                pedido.getSubtotal(),
                pedido.getValorFrete(),
                pedido.getValorTotal(),
                pedido.getStatus(),
                pedido.getDataCriacao());
    }
}
