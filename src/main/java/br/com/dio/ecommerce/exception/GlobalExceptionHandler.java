package br.com.dio.ecommerce.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Tratamento centralizado de erros. Também é um bean singleton: uma única instância
 * atende as exceções lançadas por todos os controllers.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest req) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> regraNegocio(RegraNegocioException ex, HttpServletRequest req) {
        return responder(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ErroResponse.CampoInvalido> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErroResponse.CampoInvalido(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return responder(HttpStatus.BAD_REQUEST, "Dados de entrada inválidos", req, campos);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> corpoIlegivel(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido ou mal formatado", req, List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> tipoInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String mensagem = "Valor '%s' inválido para o parâmetro '%s'".formatted(ex.getValue(), ex.getName());
        return responder(HttpStatus.BAD_REQUEST, mensagem, req, List.of());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErroResponse> conflito(ObjectOptimisticLockingFailureException ex, HttpServletRequest req) {
        return responder(HttpStatus.CONFLICT,
                "O recurso foi alterado por outra requisição. Tente novamente.", req, List.of());
    }

    private ResponseEntity<ErroResponse> responder(HttpStatus status, String mensagem, HttpServletRequest req,
                                                   List<ErroResponse.CampoInvalido> campos) {
        ErroResponse corpo = new ErroResponse(
                OffsetDateTime.now(), status.value(), status.getReasonPhrase(), mensagem, req.getRequestURI(), campos);
        return ResponseEntity.status(status).body(corpo);
    }
}
