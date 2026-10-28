package com.fitzone.domain.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Serviço de avaliação física, com valor fixo por sessão (não varia por
 * quantidade ou duração).
 */

@Entity
@DiscriminatorValue("AVALIACAO_FISICA")
public class AvaliacaoFisica extends ServicoAdicional{

    public AvaliacaoFisica(){}

    @Override
    public TipoServico getTipoServico() { return TipoServico.AVALIACAO_FISICA; }

    @Override
    public double getValorTotal() { return getTipoServico().getValor(); }
}
