package dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record AgendamentoRequest(
        @NotBlank(message = "CPF do aluno é obrigatório")
        String alunoCpf,

        @NotBlank(message = "ID do ambiente é obrigatório")
        String ambienteId,

        @NotNull(message = "Data do agendamento é obrigatória")
        @FutureOrPresent(message = "Data do agendamento não pode ser no passado")
        LocalDate dataAgendamento,

        @NotNull(message = "Hora de início é obrigatória")
        LocalTime horaInicio,

        @NotNull(message = "Hora de fim é obrigatória")
        LocalTime horaFim
) {
}
