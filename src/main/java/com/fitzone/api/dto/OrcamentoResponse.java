package com.fitzone.api.dto;

import com.fitzone.domain.model.Agendamento;

// Prévia de uma reserva: o mesmo cálculo do agendamento real, sem gravar nada
public record OrcamentoResponse(
        Integer ambienteId,
        String ambienteNome,
        long horasCobradas,
        double valorHora,
        double valorTotal
) {
    public static OrcamentoResponse from(Agendamento previa) {
        return new OrcamentoResponse(previa.getAmbiente().getId(), previa.getAmbiente().getNome(),
                previa.getHorasCobradas(), previa.getAmbiente().getValorHora(), previa.getValorTotal());
    }
}
