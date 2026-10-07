package com.fitzone.domain.exception;

public class ServicoInvalidoException extends Exception{
    
    private static final long serialVersionUID = 1L;

    /**
     * @param mensagem
     */
    
    public ServicoInvalidoException(String mensagem){
        super("Erro no serviço: " + mensagem);
    }
}
