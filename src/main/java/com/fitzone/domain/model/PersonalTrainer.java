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

    public PersonalTrainer(){}

    @Override
    public TipoServico getTipoServico() { return TipoServico.PERSONAL_TRAINER; }

    @Override
    public double getValorTotal() {
        if(getAgendamento() == null) {
            throw new IllegalStateException
            ("Personal Trainer precisa estar vinculado a um agendamento para calcular o valor.");
        }
        // as horas vêm do agendamento: a regra de duração continua só em getHorasCobradas()
        return getTipoServico().getValor() * getAgendamento().getHorasCobradas();
    }

}
