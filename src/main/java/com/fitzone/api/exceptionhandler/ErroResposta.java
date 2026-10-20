package com.fitzone.api.exceptionhandler;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Formato único de erro da API. Todo erro, de qualquer endpoint, sai neste formato.
 * "campos" só aparece em erros de validação (NON_NULL omite o campo quando é null).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResposta(
        int status,
        String titulo,
        String mensagem,
        String caminho,
        LocalDateTime dataHora,
        List<Campo> campos
) {
    public record Campo(String nome, String mensagem) { }
}