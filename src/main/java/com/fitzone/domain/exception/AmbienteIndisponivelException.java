package com.fitzone.domain.exception;

public class AmbienteIndisponivelException extends RuntimeException{
    
    /**
     * @param mensagem
     */
    
    public AmbienteIndisponivelException(String mensagem){
        super("Erro ao agendar ambiente: " + mensagem);
    }
}
