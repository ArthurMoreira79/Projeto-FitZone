package com.fitzone.api.dto;

import com.fitzone.domain.model.Agendamento;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record AgendamentoResponse(
        Integer id,
        Integer alunoId,
        String alunoNome,
        Integer ambienteId,
        String ambienteNome,
        LocalDate data,
        LocalTime horaInicio,
        LocalTime horaFim,
        long horasCobradas,
        double valorTotal,
        List<ServicoResponse> servicos
) {
    public static AgendamentoResponse from(Agendamento agendamento) {
        return new AgendamentoResponse(
                agendamento.getId(),
                agendamento.getAluno().getId(),
                agendamento.getAluno().getNome(),
                agendamento.getAmbiente().getId(),
                agendamento.getAmbiente().getNome(),
                agendamento.getDataAgendamento(),
                agendamento.getHoraInicio(),
                agendamento.getHoraFim(),
                agendamento.getHorasCobradas(),
                agendamento.getValorTotal(),
                agendamento.getServicosAdicionais().stream().map(ServicoResponse::from).toList()
        );
    }
}