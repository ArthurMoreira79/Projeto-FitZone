package com.fitzone.api.dto;

import java.math.BigDecimal;

public record RelatorioServicoDTO(
        String servico,
        int quantidade,
        BigDecimal valorTotal
) { }