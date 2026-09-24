package br.com.dio.ecommerce.exception;

/** Mapeada para HTTP 404. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String recurso, Long id) {
        super("%s com id %d não encontrado(a)".formatted(recurso, id));
    }
}
