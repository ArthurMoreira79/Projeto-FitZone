package dto;

import entidades.ServicoAdicional;

public record ServicoResponse(
        Long id,
        String tipo,
        String descricao,
        double valorTotal
) {
    public static ServicoResponse from(ServicoAdicional s) {
        return new ServicoResponse(s.getId(), s.getClass().getSimpleName(), s.getDescricao(), s.getValorTotal());
    }
}
