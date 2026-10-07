package com.fitzone.domain.exception;

public class AmbienteJaCadastradoException extends RuntimeException{
    
    /**
     * @param mensagem
     */

    public AmbienteJaCadastradoException(String mensagem){
        super("Erro ao cadastrar ambiente: " + mensagem);
    }    
}
