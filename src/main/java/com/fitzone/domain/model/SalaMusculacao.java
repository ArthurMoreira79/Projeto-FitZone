package com.fitzone.domain.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * 
 * SalaMusculacao
 * sala de musculação com valor fixo por hora
 */

@Entity
@DiscriminatorValue("MUSCULACAO")
public class SalaMusculacao extends Ambiente{

    private static final double VALOR_HORA_FIXO = 100.0;

    protected SalaMusculacao() { } // exigido pelo JPA

    /**
     * @param id gerado pelo AmbienteService (faixa 101-110)
     * @param observacoes texto livre e opcional digitado pelo usuário
     */
    public SalaMusculacao(Integer id, String observacoes){
        super(id, "Sala de Musculação " + id, VALOR_HORA_FIXO, observacoes);
    }

    @Override
    public String getTipo() { return "Sala de Musculação"; }

    @Override
    public String getDescricao() { return "Tipo: " + getTipo() + " | ID: " + getId() + " | Nome: " + getNome() + " | Valor/h: R$" + getValorHora(); }

}