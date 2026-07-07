package excecoes;

public class AmbienteIndisponivelException extends Exception{
    
    private static final long serialVersionUID = 1L;
    
    public AmbienteIndisponivelException(String mensagem){
        super("Erro ao agendar ambiente: " + mensagem);
    }
}
