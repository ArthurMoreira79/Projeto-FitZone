package com.fitzone.api.dto;

import com.fitzone.domain.model.ServicoAdicional;

public record ServicoResponse(
        Long id,
        // código estável do enum ("LOCKER"), para lógica no front; descricao é texto de exibição
        String codigo,
        String descricao,
        double valorTotal
) {
    // getDescricao() e getValorTotal() são polimórficos: cada serviço calcula o seu
    public static ServicoResponse from(ServicoAdicional servico) {
        return new ServicoResponse(servico.getId(), servico.getTipoServico().name(),
                servico.getDescricao(), servico.getValorTotal());
    }
}
