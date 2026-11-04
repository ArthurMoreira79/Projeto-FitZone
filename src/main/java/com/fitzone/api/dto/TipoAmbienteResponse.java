package com.fitzone.api.dto;

import java.math.BigDecimal;

import com.fitzone.domain.model.TipoAmbiente;

public record TipoAmbienteResponse(
        String codigo,
        String nome,
        BigDecimal valorHora,
        int inicioFaixa,
        int fimFaixa
) {
    public static TipoAmbienteResponse from(TipoAmbiente tipo) {
        return new TipoAmbienteResponse(tipo.name(), tipo.getNome(), tipo.getValorHora(),
                tipo.getInicioFaixa(), tipo.getFimFaixa());
    }
}