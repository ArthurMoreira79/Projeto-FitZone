package com.fitzone.domain.exception;

public class LimiteAmbienteExcedidoException extends RuntimeException{
    
    /**
     * @param mensagem
     */

    public LimiteAmbienteExcedidoException(String mensagem) {
        super("Erro ao cadastrar ambiente: " + mensagem);
    }
}
