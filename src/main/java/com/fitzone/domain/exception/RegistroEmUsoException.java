package com.fitzone.domain.exception;

/**
 * O registro não pode ser excluído porque outros dependem dele
 * (ex.: aluno ou ambiente com agendamentos). Vira 409 no ApiExceptionHandler.
 */
public class RegistroEmUsoException extends RuntimeException {

    public RegistroEmUsoException(String mensagem) {
        super(mensagem);
    }
}
