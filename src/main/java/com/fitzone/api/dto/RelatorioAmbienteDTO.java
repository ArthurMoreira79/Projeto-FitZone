package com.fitzone.api.dto;

public record RelatorioAmbienteDTO(
        Integer ambienteId,
        String nome,
        String tipo,
        int quantidadeAgendamentos,
        long horasCobradas
) { }