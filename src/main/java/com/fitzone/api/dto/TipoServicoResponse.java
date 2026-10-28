package com.fitzone.api.dto;

import com.fitzone.domain.model.TipoServico;

public record TipoServicoResponse(
        String codigo,
        String nome,
        double valor,
        // FIXO, POR_HORA ou POR_UNIDADE: o front usa para explicar o preço e para saber quando pedir quantidade
        String cobranca
) {
    public static TipoServicoResponse from(TipoServico tipo) {
        return new TipoServicoResponse(tipo.name(), tipo.getNome(), tipo.getValor(), tipo.getCobranca().name());
    }
}
