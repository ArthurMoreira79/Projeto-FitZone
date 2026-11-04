package com.fitzone.api.dto;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.util.Map;

public record RelatorioFaturamentoDTO(
        Map<LocalDate, BigDecimal> porDia,
        Map<String, BigDecimal> porAmbiente,
        Map<String, BigDecimal> porAluno,
        BigDecimal total
) { }