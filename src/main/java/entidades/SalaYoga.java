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

    protected SalaYoga() { } // exigido pelo JPA

    /**
     * @param id auto-gerado pelo AdministradorSistema (faixa 201-220)
     * @param observacoes texto livre e opcional digitado pelo usuário
     */
    public SalaYoga(String id, String observacoes){
        super(id, "Sala de Yoga " + id, VALOR_HORA_FIXO, observacoes);
    }

    @Override
    public String getTipo() { return "Sala de Yoga"; }

    @Override
    public String getDescricao() { return "Tipo: " + getTipo() + " | ID: " + getId() + " | Nome: " + getNome() + " | Valor/h: R$" + getValorHora(); }
}