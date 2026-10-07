package com.fitzone.domain.exception;

public class AmbienteNaoEncontradoException extends RuntimeException{
    
    /**
     * @param mensagem
     */

    public AmbienteNaoEncontradoException(String mensagem) {
        super("Erro ao buscar ambientes: " + mensagem);
    }
}
