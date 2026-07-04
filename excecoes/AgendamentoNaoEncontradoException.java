package excecoes;

public class AgendamentoNaoEncontradoException extends Exception{
    
    private static final long serialVersionUID = 1L;
    
    public AgendamentoNaoEncontradoException(String mensagem){
        super("Erro ao processar agendamento: " + mensagem);
    }
}
