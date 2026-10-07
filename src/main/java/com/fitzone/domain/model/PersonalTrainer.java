package com.fitzone.domain.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Serviço de acompanhamento com personal trainer, com valor fixo por hora
 * de agendamento.
 */

@Entity
@DiscriminatorValue("PERSONAL_TRAINER")
public class PersonalTrainer extends ServicoAdicional{
    
    private static final double VALOR_POR_HORA = 50.0;

    public PersonalTrainer(){}

    @Override
    public String getDescricao() { return "Personal Trainer"; }

    @Override
    public double getValorTotal() {
        if(getAgendamento() == null) {
            throw new IllegalStateException
            ("Personal Trainer precisa estar vinculado a um agendamento para calcular o valor.");
        }
        return VALOR_POR_HORA * getAgendamento().getHorasCobradas();
    }

}
