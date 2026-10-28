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

    protected SalaMusculacao() { } // exigido pelo JPA

    /**
     * @param id gerado pelo AmbienteService (faixa 101-110)
     * @param observacoes texto livre e opcional digitado pelo usuário
     */
    public SalaMusculacao(Integer id, String observacoes){
        super(id, TipoAmbiente.MUSCULACAO.getNome() + " " + id, TipoAmbiente.MUSCULACAO.getValorHora(), observacoes);
    }

    @Override
    public TipoAmbiente getTipoAmbiente() { return TipoAmbiente.MUSCULACAO; }

    @Override
    public String getDescricao() { return "Tipo: " + getTipo() + " | ID: " + getId() + " | Nome: " + getNome() + " | Valor/h: R$" + getValorHora(); }

}