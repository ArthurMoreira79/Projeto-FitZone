package com.fitzone.api.dto;

import com.fitzone.domain.model.Ambiente;

public record AmbienteResponse(
        Integer id,
        String tipo,
        String nome,
        double valorHora,
        String observacoes
) {
    // getTipo() é polimórfico: cada subclasse devolve o próprio nome ("Sala de Yoga", "Piscina"...)
    public static AmbienteResponse from(Ambiente ambiente) {
        return new AmbienteResponse(ambiente.getId(), ambiente.getTipo(), ambiente.getNome(),
                ambiente.getValorHora(), ambiente.getObservacoes());
    }
}