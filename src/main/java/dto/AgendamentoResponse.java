package dto;

import entidades.Agendamento;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record AgendamentoResponse(
        int id,
        AlunoResponse aluno,
        AmbienteResponse ambiente,
        LocalDate dataAgendamento,
        LocalTime horaInicio,
        LocalTime horaFim,
        double valorTotal,
        List<ServicoResponse> servicos
) {
    public static AgendamentoResponse from(Agendamento a) {
        return new AgendamentoResponse(
                a.getId(),
                AlunoResponse.from(a.getAluno()),
                AmbienteResponse.from(a.getAmbiente()),
                a.getDataAgendamento(),
                a.getHoraInicio(),
                a.getHoraFim(),
                a.getValorTotal(),
                a.getServicosAdicionais().stream().map(ServicoResponse::from).toList()
        );
    }
}
