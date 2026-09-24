package br.com.dio.ecommerce.dto;

import br.com.dio.ecommerce.singleton.ConfiguracaoLoja;

import java.math.BigDecimal;

public record ConfiguracaoLojaResponse(
        String nomeLoja,
        String moeda,
        BigDecimal valorMinimoPedido,
        BigDecimal taxaFrete
) {

    public static ConfiguracaoLojaResponse from(ConfiguracaoLoja config) {
        return new ConfiguracaoLojaResponse(
                config.getNomeLoja(),
                config.getMoeda(),
                config.getValorMinimoPedido(),
                config.getTaxaFrete());
    }
}
