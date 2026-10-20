package com.fitzone.api.dto;

import jakarta.validation.constraints.Size;

public record AmbienteObservacoesRequest(

        @Size(max = 200, message = "As observações devem ter no máximo 200 caracteres")
        String observacoes
) { }