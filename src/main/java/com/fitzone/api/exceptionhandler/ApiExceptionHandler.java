package com.fitzone.api.exceptionhandler;

import com.fitzone.domain.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice 
public class ApiExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    // 404 - recurso não encontrado

    @ExceptionHandler ({
        AlunoNaoEncontradoException.class,
        AmbienteNaoEncontradoException.class,
        AgendamentoNaoEncontradoException.class
    })
    public ResponseEntity<ErroResposta> naoEncontrado(RuntimeException ex, HttpServletRequest req) {
        return responder(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage(), req, null);
    }

    // URL que não corresponde a nenhum endpoint (ex: GET /api/alunoss)
    @ExceptionHandler (NoResourceFoundException.class)
    public ResponseEntity<ErroResposta> rotaInexistente(NoResourceFoundException ex, HttpServletRequest req) {
        return responder(HttpStatus.NOT_FOUND, "Endpoint não encontrado", "Não existe endpoint para " + req.getMethod() + " " + req.getRequestURI() + ".", req, null);
    }

    // 409 - conflito com o estado atual do sistema

    @ExceptionHandler ({
        AlunoJaCadastradoException.class,
        AmbienteJaCadastradoException.class,
        AmbienteIndisponivelException.class,
        LimiteAmbienteExcedidoException.class
    })
    public ResponseEntity<ErroResposta> conflito(RuntimeException ex, HttpServletRequest req) {
        return responder(HttpStatus.CONFLICT, "Conflito", ex.getMessage(), req, null);
    }

    //O banco recusou a operação: FK em uso (ON DELETE RESTRICT), UNIQUE ou CHECK violados.
    //Normalmente os servicos barram antes; isto é a última linha de defesa.
    @ExceptionHandler (DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> integridade(DataIntegrityViolationException ex, HttpServletRequest req) {
        return responder(HttpStatus.CONFLICT, "Conflito de integridade", "A operação não pode ser concluída: o registro está em uso ou viola uma regra do banco de dados.", req, null);
    }

    // 400 - requisição inválida

    @ExceptionHandler ({
        HorarioInvalidoException.class,
        ServicoInvalidoException.class
    })
    public ResponseEntity<ErroResposta> regraInvalida(RuntimeException ex, HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST, "Requisição inválida", ex.getMessage(), req, null);
    }

    // Falhas do Bean Validation (@Valid): devolve a lista de campos com problema
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> validacao(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ErroResposta.Campo> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> new ErroResposta.Campo(erro.getField(), erro.getDefaultMessage()))
                .toList();

        return responder(HttpStatus.BAD_REQUEST, "Dados inválidos",
                "Um ou mais campos estão inválidos. Corrija e tente novamente.", req, campos);
    }

    // JSON malformado, data/hora fora do formato ou enum inexistente (ex.: "tipo": "BOXE")
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> jsonInvalido(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido",
                "Verifique a sintaxe do JSON e o formato dos campos: datas AAAA-MM-DD, horas HH:mm "
                        + "e tipos com um dos valores aceitos.", req, null);
    }

    // Parâmetro da URL com tipo errado (ex.: GET /api/alunos/abc)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResposta> tipoParametro(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST, "Parâmetro inválido",
                "O parâmetro '" + ex.getName() + "' recebeu o valor '" + ex.getValue()
                        + "', que não é do tipo esperado.", req, null);
    }

    // Parâmetro de query obrigatório ausente (ex.: GET /api/alunos/busca sem ?cpf=)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResposta> parametroAusente(MissingServletRequestParameterException ex, HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST, "Parâmetro obrigatório ausente",
                "Informe o parâmetro '" + ex.getParameterName() + "'.", req, null);
    }

    // 405 / 415

    //Verbo não suportado na rota (ex: DELETE api/alunos, sem id)
    @ExceptionHandler (HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResposta> metodoNaoPermitido(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED, "Método não permitido", "O método " + ex.getMethod() + " não é suportado neste endpoint.", req, null);
    }

    //Corpo enviado em formato diferente de JSON
    @ExceptionHandler (HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroResposta> formatoNaoSuportado(HttpMediaTypeNotSupportedException ex, HttpServletRequest req) {
        return responder(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Formato não suportado", "Envie o corpo da requisição em JSON (Content-Type: application/json).", req, null);
    }

    // 500 - rede de proteção

    //Qualquer erro não previsto acima. O detalhe técnico vai para o LOG do servidor, nunca para o cliente (stack trace exposto ajuda quem quer atacar a API).
    @ExceptionHandler (Exception.class)
    public ResponseEntity<ErroResposta> erroInesperado(Exception ex, HttpServletRequest req) {
        log.error("Erro inesperado em {} {}", req.getMethod(), req.getRequestURI(), ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", "Ocorreu um erro inesperado no servidor. Tente novamente mais tarde.", req, null);
    }

    private ResponseEntity<ErroResposta> responder(HttpStatus status, String titulo, String mensagem, HttpServletRequest req, List<ErroResposta.Campo> campos) {
        ErroResposta corpo = new ErroResposta(status.value(), titulo, mensagem, req.getRequestURI(), LocalDateTime.now(), campos);
        return ResponseEntity.status(status).body(corpo);
    }
}