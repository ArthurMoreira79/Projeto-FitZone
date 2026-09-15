package web;

import excecoes.AgendamentoNaoEncontradoException;
import excecoes.AlunoJaCadastradoException;
import excecoes.AlunoNaoEncontradoException;
import excecoes.AmbienteIndisponivelException;
import excecoes.AmbienteJaCadastradoException;
import excecoes.AmbienteNaoEncontradoException;
import excecoes.FalhaPersistenciaException;
import excecoes.LimiteAmbienteExcedidoException;
import excecoes.ServicoInvalidoException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({
            AlunoNaoEncontradoException.class,
            AmbienteNaoEncontradoException.class,
            AgendamentoNaoEncontradoException.class
    })
    ResponseEntity<ApiErrorResponse> naoEncontrado(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler({
            AlunoJaCadastradoException.class,
            AmbienteJaCadastradoException.class,
            AmbienteIndisponivelException.class,
            LimiteAmbienteExcedidoException.class
    })
    ResponseEntity<ApiErrorResponse> conflito(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(ServicoInvalidoException.class)
    ResponseEntity<ApiErrorResponse> requisicaoInvalida(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> validacaoInvalida(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, mensagem, req);
    }

    @ExceptionHandler(FalhaPersistenciaException.class)
    ResponseEntity<ApiErrorResponse> falhaPersistencia(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), req);
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String mensagem, HttpServletRequest req) {
        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(), status.value(), status.getReasonPhrase(), mensagem, req.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
