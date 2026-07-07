package excecoes;

public class AmbienteJaCadastradoException extends Exception{
    
    private static final long serialVersionUID = 1L;

    public AmbienteJaCadastradoException(String mensagem){
        super("Erro ao cadastrar ambiente: " + mensagem);
    }    
}
