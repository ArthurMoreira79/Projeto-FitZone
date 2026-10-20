package com.fitzone.api.dto;

import com.fitzone.domain.exception.ServicoInvalidoException;
import com.fitzone.domain.model.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ServicoRequest(

        @NotNull(message = "O tipo do serviço é obrigatório (AVALIACAO_FISICA, NUTRICIONISTA, PERSONAL_TRAINER ou LOCKER)")
        TipoServico tipo,

        // só usado pelo LOCKER; ignorado nos demais tipos
        @Positive(message = "A quantidade de lockers deve ser maior que zero")
        Integer quantidade
) {
    public ServicoAdicional toEntity() {
        return switch (tipo) {
            case AVALIACAO_FISICA -> new AvaliacaoFisica();
            case NUTRICIONISTA    -> new Nutricionista();
            case PERSONAL_TRAINER -> new PersonalTrainer();
            case LOCKER -> {
                // regra que envolve dois campos (tipo + quantidade): Bean Validation
                // de campo único não consegue expressar, então fica aqui
                if (quantidade == null) {
                    throw new ServicoInvalidoException("Informe a quantidade de lockers.");
                }
                yield new LockerAcademia(quantidade);
            }
        };
    }
}