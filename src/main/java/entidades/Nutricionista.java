package entidades;

public class Nutricionista implements ServicoAdicional{
    
    private static final long serialVersionUID = 1L;

    private static final double VALOR_CONSULTA = 80.0;

    public Nutricionista(){}

    @Override
    public String getDescricao() { return "Nutricionista"; }

    @Override
    public double getValorTotal() { return VALOR_CONSULTA; }
}
