package com.fitzone.api.dto;

public record RelatorioServicoDTO(
        String servico,
        int quantidade,
        double valorTotal
) { }