package excecoes;

public class FalhaPersistenciaException extends Exception{
    
    private static final long serialVersionUID = 1L;

    public FalhaPersistenciaException(String mensagem){
        super("Erro crítico ao salvar/ler arquivos: " + mensagem);
    }
}
