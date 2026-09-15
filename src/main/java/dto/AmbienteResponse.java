package dto;

import entidades.Ambiente;

public record AmbienteResponse(
        String id,
        String tipo,
        String nome,
        double valorHora,
        String observacoes
) {
    public static AmbienteResponse from(Ambiente a) {
        return new AmbienteResponse(a.getId(), a.getTipo(), a.getNome(), a.getValorHora(), a.getObservacoes());
    }
}
