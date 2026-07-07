package entidades;

public class SalaCrossfit extends Ambiente{
    
    private static final long serialVersionUID = 1L;

    private static final double VALOR_HORA_FIXO = 100.0;

    public SalaCrossfit(String id, String nome){
        super(id, nome, VALOR_HORA_FIXO);
    }

    @Override
    public String getTipo() { return "Sala de Crossfit"; }

    @Override
    public String getDescricao() { return "Tipo: " + getTipo() + " | ID: " + getId() + " | Nome: " + getNome() + " | Valor/h: R$" + getValorHora(); }
}
