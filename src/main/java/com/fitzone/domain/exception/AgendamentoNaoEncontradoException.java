package com.fitzone.domain.exception;

public class AgendamentoNaoEncontradoException extends Exception{
    
    private static final long serialVersionUID = 1L;

    /**
     * @param mensagem
     */
    
    public AgendamentoNaoEncontradoException(String mensagem){
        super("Erro ao processar agendamento: " + mensagem);
    }
}
