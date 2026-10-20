package com.fitzone.api.dto;

import java.time.LocalDate;
import java.util.Map;

public record RelatorioFaturamentoDTO(
        Map<LocalDate, Double> porDia,
        Map<String, Double> porAmbiente,
        Map<String, Double> porAluno,
        double total
) { }