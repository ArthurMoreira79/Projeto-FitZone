package com.fitzone.domain.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * 
 * SalaYoga
 * sala de yoga com valor fixo por hora
 */

@Entity
@DiscriminatorValue("YOGA")
public class SalaYoga extends Ambiente {

    protected SalaYoga() { } // exigido pelo JPA

    /**
     * @param id gerado pelo AmbienteService (faixa 201-210)
     * @param observacoes texto livre e opcional digitado pelo usuário
     */
    public SalaYoga(Integer id, String observacoes) {
        super(id, TipoAmbiente.YOGA.getNome() + " " + id, TipoAmbiente.YOGA.getValorHora(), observacoes);
    }

    @Override
    public TipoAmbiente getTipoAmbiente() { return TipoAmbiente.YOGA; }

    @Override
    public String getDescricao() { return "Tipo: " + getTipo() + " | ID: " + getId() + " | Nome: " + getNome() + " | Valor/h: R$" + getValorHora(); }
}
