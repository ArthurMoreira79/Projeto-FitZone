package com.fitzone.api.dto;

import java.util.List;

public record RelatorioAlunoDTO(
        Integer alunoId,
        String nome,
        int quantidadeAgendamentos,
        long horasCobradas,
        double valorTotal,
        List<AgendamentoResponse> agendamentos
) { }