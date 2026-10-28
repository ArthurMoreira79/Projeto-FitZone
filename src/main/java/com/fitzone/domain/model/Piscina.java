package com.fitzone.domain.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * 
 * Piscina
 * piscina com valor fixo por hora
 */

@Entity
@DiscriminatorValue("PISCINA")
public class Piscina extends Ambiente{

    protected Piscina() { } // exigido pelo JPA

    /**
     * @param id gerado pelo AmbienteService (faixa 401-410)
     * @param observacoes texto livre e opcional digitado pelo usuário
     */
    public Piscina(Integer id, String observacoes){
        super(id, TipoAmbiente.PISCINA.getNome() + " " + id, TipoAmbiente.PISCINA.getValorHora(), observacoes);
    }

    @Override
    public TipoAmbiente getTipoAmbiente() { return TipoAmbiente.PISCINA; }

    @Override
    public String getDescricao() { return "Tipo: " + getTipo() + " | ID: " + getId() + " | Nome: " + getNome() + " | Valor/h: R$" + getValorHora(); }
}