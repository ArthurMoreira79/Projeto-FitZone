package com.fitzone.api.dto;

import java.math.BigDecimal;

public record RelatorioAmbienteDTO(
        Integer ambienteId,
        String nome,
        String tipo,
        // código do enum do tipo ("YOGA"): o front usa para a anilha ao lado do nome
        String codigo,
        int quantidadeAgendamentos,
        long horasCobradas,
        // soma do valor total dos agendamentos do ambiente (serviços incluídos)
        BigDecimal valorTotal
) { }
