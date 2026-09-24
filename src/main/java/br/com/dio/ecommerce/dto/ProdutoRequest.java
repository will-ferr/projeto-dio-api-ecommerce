package br.com.dio.ecommerce.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProdutoRequest(
        @NotBlank @Size(max = 150) String nome,
        @Size(max = 1000) String descricao,
        @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal preco,
        @NotNull @PositiveOrZero Integer estoque,
        @NotNull Long categoriaId
) {
}
