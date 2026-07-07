package entidades;

public class AvaliacaoFisica implements ServicoAdicional{
    
    private static final long serialVersionUID = 1L;

    private static final double VALOR_AVALIACAO = 70.0;

    public AvaliacaoFisica(){}

    @Override
    public String getDescricao() { return "Avaliação Física"; }

    @Override
    public double getValorTotal() { return VALOR_AVALIACAO; }
}
