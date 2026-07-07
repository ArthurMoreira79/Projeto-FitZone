package entidades;

public class Piscina extends Ambiente{
    
    private static final long serialVersionUID = 1L;

    private static final double VALOR_HORA_FIXO = 80.0;

    public Piscina(String id, String nome){
        super(id, nome, VALOR_HORA_FIXO);
    }

    @Override
    public String getTipo() { return "Piscina"; }

    @Override
    public String getDescricao() { return "Tipo: " + getTipo() + " | ID: " + getId() + " | Nome: " + getNome() + " | Valor/h: R$" + getValorHora(); }
}
