package com.fitzone.domain.exception;

public class HorarioInvalidoException extends RuntimeException{
    

    /**
     * @param mensagem
     */
    
    public HorarioInvalidoException(String mensagem) {
        super("Erro ao agendar ambiente: " + mensagem);
    }
}
