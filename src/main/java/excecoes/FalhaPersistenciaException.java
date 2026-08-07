package excecoes;

public class FalhaPersistenciaException extends Exception{
    
    private static final long serialVersionUID = 1L;

    /**
     * @param mensagem
     */

    public FalhaPersistenciaException(String mensagem){
        super("Erro crítico ao acessar o banco de dados: " + mensagem);
    }
}
