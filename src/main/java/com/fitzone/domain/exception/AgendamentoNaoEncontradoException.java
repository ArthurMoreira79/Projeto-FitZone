package com.fitzone.domain.exception;

public class AgendamentoNaoEncontradoException extends RuntimeException{
    
    /**
     * @param mensagem
     */
    
    public AgendamentoNaoEncontradoException(String mensagem){
        super("Erro ao processar agendamento: " + mensagem);
    }
}
