package com.fitzone.domain.exception;

public class AmbienteIndisponivelException extends Exception{
    
    private static final long serialVersionUID = 1L;

    /**
     * @param mensagem
     */
    
    public AmbienteIndisponivelException(String mensagem){
        super("Erro ao agendar ambiente: " + mensagem);
    }
}
