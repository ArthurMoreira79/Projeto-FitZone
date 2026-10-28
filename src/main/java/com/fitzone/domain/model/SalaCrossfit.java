package com.fitzone.domain.model;

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

    protected SalaCrossfit() { } // exigido pelo JPA

    /**
     * @param id gerado pelo AmbienteService (faixa 301-310)
     * @param observacoes texto livre e opcional digitado pelo usuário
     */
    public SalaCrossfit(Integer id, String observacoes){
        super(id, TipoAmbiente.CROSSFIT.getNome() + " " + id, TipoAmbiente.CROSSFIT.getValorHora(), observacoes);
    }

    @Override
    public TipoAmbiente getTipoAmbiente() { return TipoAmbiente.CROSSFIT; }

    @Override
    public String getDescricao() { return "Tipo: " + getTipo() + " | ID: " + getId() + " | Nome: " + getNome() + " | Valor/h: R$" + getValorHora(); }
}