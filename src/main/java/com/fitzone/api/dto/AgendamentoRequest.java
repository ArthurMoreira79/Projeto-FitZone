package com.fitzone.api.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record AgendamentoRequest(

        @NotNull(message = "O id do aluno é obrigatório")
        Integer alunoId,

        @NotNull(message = "O id do ambiente é obrigatório")
        Integer ambienteId,

        @NotNull(message = "A data é obrigatória")
        @FutureOrPresent(message = "Não é possível agendar em uma data passada")
        LocalDate data,

        @NotNull(message = "A hora de início é obrigatória")
        LocalTime horaInicio,

        @NotNull(message = "A hora de fim é obrigatória")
        LocalTime horaFim
) { }