package com.fitzone.domain.exception;

/** Serviço adicional inexistente ou de outro agendamento. Vira 404 no ApiExceptionHandler. */
public class ServicoNaoEncontradoException extends RuntimeException {

    public ServicoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
