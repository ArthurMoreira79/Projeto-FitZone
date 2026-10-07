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
public class SalaYoga extends Ambiente{

    private static final double VALOR_HORA_FIXO = 70.0;

    protected SalaYoga() { } // exigido pelo JPA

    /**
     * @param id gerado pelo AmbienteService (faixa 201-210)
     * @param observacoes texto livre e opcional digitado pelo usuário
     */
    public SalaYoga(Integer id, String observacoes){
        super(id, "Sala de Yoga " + id, VALOR_HORA_FIXO, observacoes);
    }

    @Override
    public String getTipo() { return "Sala de Yoga"; }

    @Override
    public String getDescricao() { return "Tipo: " + getTipo() + " | ID: " + getId() + " | Nome: " + getNome() + " | Valor/h: R$" + getValorHora(); }
}