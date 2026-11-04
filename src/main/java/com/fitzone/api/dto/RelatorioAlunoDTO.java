package com.fitzone.api.dto;

import java.math.BigDecimal;

import java.util.List;

public record RelatorioAlunoDTO(
        Integer alunoId,
        String nome,
        int quantidadeAgendamentos,
        long horasCobradas,
        BigDecimal valorTotal,
        List<AgendamentoResponse> agendamentos
) { }