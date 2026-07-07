package entidades;

public class PersonalTrainer implements ServicoAdicional{
    
    private static final long serialVersionUID = 1L;

    private static final double VALOR_POR_HORA = 50.0;

    public PersonalTrainer(){}

    @Override
    public String getDescricao() { return "Personal Trainer"; }

    @Override
    public double getValorTotal() { return VALOR_POR_HORA; }

}
