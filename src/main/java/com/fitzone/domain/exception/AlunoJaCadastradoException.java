package com.fitzone.domain.exception;

public class AlunoJaCadastradoException extends Exception{
    
    private static final long serialVersionUID = 1L;

    /**
     * @param mensagem
     */
    
    public AlunoJaCadastradoException(String mensagem){
        super("Erro ao cadastrar aluno: " + mensagem);
    }
}
