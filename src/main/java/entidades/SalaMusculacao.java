package entidades;

public class SalaMusculacao extends Ambiente{

    private static final long serialVersionUID = 1L;

    private static final double VALOR_HORA_FIXO = 100.0;

    public SalaMusculacao(String id, String nome){
        super(id, nome, VALOR_HORA_FIXO);
    }

    @Override
    public String getTipo() { return "Sala de Musculação"; }

    @Override
    public String getDescricao() { return "Tipo: " + getTipo() + " | ID: " + getId() + " | Nome: " + getNome() + " | Valor/h: R$" + getValorHora(); }

}
