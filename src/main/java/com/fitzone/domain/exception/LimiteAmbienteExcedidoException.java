package com.fitzone.domain.exception;

public class LimiteAmbienteExcedidoException extends Exception{
    
    private static final long serialVersionUID = 1L;

    /**
     * @param mensagem
     */

    public LimiteAmbienteExcedidoException(String mensagem) {
        super("Erro ao cadastrar ambiente: " + mensagem);
    }
}
