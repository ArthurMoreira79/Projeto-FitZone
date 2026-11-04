package com.fitzone.domain.model;

import java.math.BigDecimal;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Serviço de consulta com nutricionista, com valor fixo por consulta.
 */

@Entity
@DiscriminatorValue("NUTRICIONISTA")
public class Nutricionista extends ServicoAdicional{

    public Nutricionista(){}

    @Override
    public TipoServico getTipoServico() { return TipoServico.NUTRICIONISTA; }

    @Override
    public BigDecimal getValorTotal() { return getTipoServico().getValor(); }
}
