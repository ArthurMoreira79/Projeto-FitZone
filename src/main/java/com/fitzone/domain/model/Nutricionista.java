package com.fitzone.domain.model;

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
    public double getValorTotal() { return getTipoServico().getValor(); }
}
