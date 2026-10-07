package com.fitzone.domain.exception;

public class AlunoNaoEncontradoException extends RuntimeException{
    
    /**
     * @param mensagem
     */
    
    public AlunoNaoEncontradoException(String mensagem){
        super("Erro ao buscar aluno: " + mensagem);
    }
}
