package br.com.dio.ecommerce.exception;

/** Requisição válida sintaticamente, mas que viola uma regra de negócio. Mapeada para HTTP 422. */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
