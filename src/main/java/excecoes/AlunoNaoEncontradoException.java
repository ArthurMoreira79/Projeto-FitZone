package excecoes;

public class AlunoNaoEncontradoException extends Exception{
    
    private static final long serialVersionUID = 1L;
    
    public AlunoNaoEncontradoException(String mensagem){
        super("Erro ao buscar aluno: " + mensagem);
    }
}
