package br.com.dio.ecommerce.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.List;

/** Corpo padronizado de todas as respostas de erro da API. */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErroResponse(
        OffsetDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        String path,
        List<CampoInvalido> campos
) {

    public record CampoInvalido(String campo, String mensagem) {
    }
}
