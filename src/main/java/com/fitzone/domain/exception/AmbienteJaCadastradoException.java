package com.fitzone.domain.exception;

public class AmbienteJaCadastradoException extends Exception{
    
    private static final long serialVersionUID = 1L;

    /**
     * @param mensagem
     */

    public AmbienteJaCadastradoException(String mensagem){
        super("Erro ao cadastrar ambiente: " + mensagem);
    }    
}
