package com.fitzone.api.dto;

import java.math.BigDecimal;

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
        // código do enum do tipo ("YOGA"): o front usa para pintar a anilha do ambiente
        String ambienteCodigo,
        LocalDate data,
        LocalTime horaInicio,
        LocalTime horaFim,
        long horasCobradas,
        BigDecimal valorTotal,
        List<ServicoResponse> servicos
) {
    public static AgendamentoResponse from(Agendamento agendamento) {
        return new AgendamentoResponse(
                agendamento.getId(),
                agendamento.getAluno().getId(),
                agendamento.getAluno().getNome(),
                agendamento.getAmbiente().getId(),
                agendamento.getAmbiente().getNome(),
                agendamento.getAmbiente().getTipoAmbiente().name(),
                agendamento.getDataAgendamento(),
                agendamento.getHoraInicio(),
                agendamento.getHoraFim(),
                agendamento.getHorasCobradas(),
                agendamento.getValorTotal(),
                agendamento.getServicosAdicionais().stream().map(ServicoResponse::from).toList()
        );
    }
}