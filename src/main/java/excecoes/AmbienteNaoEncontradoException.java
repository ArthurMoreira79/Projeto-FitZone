package excecoes;

public class AmbienteNaoEncontradoException extends Exception{

    private static final long serialVersionUID = 1L;

    /**
     * @param mensagem
     */

    public AmbienteNaoEncontradoException(String mensagem){
        super("Erro ao buscar ambiente: " + mensagem);
    }
}
