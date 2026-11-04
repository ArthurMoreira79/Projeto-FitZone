package com.fitzone.api.dto;

import java.math.BigDecimal;

import com.fitzone.domain.model.Ambiente;

public record AmbienteResponse(
        Integer id,
        String codigo,
        String tipo,
        String nome,
        BigDecimal valorHora,
        String observacoes
) {
    // codigo é o nome do enum ("YOGA"): estável, feito para o front usar em lógica.
    // tipo é o texto de exibição ("Sala de Yoga"): pode mudar sem quebrar nada.
    public static AmbienteResponse from(Ambiente ambiente) {
        return new AmbienteResponse(ambiente.getId(), ambiente.getTipoAmbiente().name(), ambiente.getTipo(), ambiente.getNome(),
                ambiente.getValorHora(), ambiente.getObservacoes());
    }
}