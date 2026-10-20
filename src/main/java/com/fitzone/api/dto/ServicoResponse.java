package com.fitzone.api.dto;

import com.fitzone.domain.model.ServicoAdicional;

public record ServicoResponse(
        Long id,
        String descricao,
        double valorTotal
) {
    // getDescricao() e getValorTotal() são polimórficos: cada serviço calcula o seu
    public static ServicoResponse from(ServicoAdicional servico) {
        return new ServicoResponse(servico.getId(), servico.getDescricao(), servico.getValorTotal());
    }
}