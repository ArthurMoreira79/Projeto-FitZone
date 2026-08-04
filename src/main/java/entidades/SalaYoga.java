package entidades;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * 
 * SalaYoga
 * sala de yoga com valor fixo por hora
 */

@Entity
@DiscriminatorValue("YOGA")
public class SalaYoga extends Ambiente{
    
    private static final long serialVersionUID = 1L;

    private static final double VALOR_HORA_FIXO = 70.0;

    protected SalaYoga() {}

    public SalaYoga(String id, String nome){
        super(id, nome, VALOR_HORA_FIXO);
    }

    @Override
    public String getTipo() { return "Sala de Yoga"; }

    @Override
    public String getDescricao() { return "Tipo: " + getTipo() + " | ID: " + getId() + " | Nome: " + getNome() + " | Valor/h: R$" + getValorHora(); }
}
