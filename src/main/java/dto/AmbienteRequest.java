package dto;

import entidades.TipoAmbiente;
import jakarta.validation.constraints.NotNull;

public record AmbienteRequest(
        @NotNull(message = "Tipo de ambiente é obrigatório")
        TipoAmbiente tipo,

        String observacoes
) {
}
