package com.fitzone.domain.exception;

public class AlunoJaCadastradoException extends RuntimeException{
    
    /**
     * @param mensagem
     */
    
    public AlunoJaCadastradoException(String mensagem){
        super("Erro ao cadastrar aluno: " + mensagem);
    }
}
