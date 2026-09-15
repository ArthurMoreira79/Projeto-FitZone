package dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ServicoRequest(
        @NotBlank(message = "Tipo de serviço é obrigatório")
        String tipo,

        @Positive(message = "Quantidade deve ser positiva")
        Integer quantidade
) {
}
