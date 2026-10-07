package com.fitzone.domain.exception;

public class ServicoInvalidoException extends RuntimeException{
    
    /**
     * @param mensagem
     */
    
    public ServicoInvalidoException(String mensagem){
        super("Erro no serviço: " + mensagem);
    }
}
