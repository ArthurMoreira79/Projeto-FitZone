package com.fitzone.api.dto;

import com.fitzone.domain.model.TipoAmbiente;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// O cliente só escolhe o tipo e as observações: id, nome e valor/hora são gerados pelo AmbienteService
public record AmbienteRequest(

        @NotNull(message = "O tipo é obrigatório (MUSCULACAO, YOGA, CROSSFIT ou PISCINA)")
        TipoAmbiente tipo,

        @Size(max = 200, message = "As observações devem ter no máximo 200 caracteres")
        String observacoes
) { }