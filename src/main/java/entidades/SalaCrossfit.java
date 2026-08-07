package entidades;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * 
 * SalaCrossfit
 * sala de crossfit com valor fixo por hora
 */

@Entity
@DiscriminatorValue("CROSSFIT")
public class SalaCrossfit extends Ambiente{
    
    private static final long serialVersionUID = 1L;

    private static final double VALOR_HORA_FIXO = 100.0;

    protected SalaCrossfit() { } // exigido pelo JPA

    /**
     * @param id auto-gerado pelo AdministradorSistema (faixa 301-320)
     * @param observacoes texto livre e opcional digitado pelo usuário
     */
    public SalaCrossfit(String id, String observacoes){
        super(id, "Sala de Crossfit " + id, VALOR_HORA_FIXO, observacoes);
    }

    @Override
    public String getTipo() { return "Sala de Crossfit"; }

    @Override
    public String getDescricao() { return "Tipo: " + getTipo() + " | ID: " + getId() + " | Nome: " + getNome() + " | Valor/h: R$" + getValorHora(); }
}