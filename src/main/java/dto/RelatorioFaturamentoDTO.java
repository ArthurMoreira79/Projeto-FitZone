package dto;

import java.util.Map;

public record RelatorioFaturamentoDTO(
        Map<String, Double> porDia,
        Map<String, Double> porAmbiente,
        Map<String, Double> porAluno,
        double total
) {
}
