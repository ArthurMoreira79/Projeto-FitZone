package service;

import dto.RelatorioAmbienteDTO;
import dto.RelatorioFaturamentoDTO;
import dto.RelatorioServicoDTO;
import entidades.Agendamento;
import entidades.ServicoAdicional;
import org.springframework.stereotype.Service;
import repository.AgendamentoRepository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RelatorioService {

    private final AgendamentoRepository agendamentoRepository;

    public RelatorioService(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    public List<Agendamento> relatorioPorAluno(String cpf) {
        List<Agendamento> lista = new ArrayList<>();
        for (Agendamento a : agendamentoRepository.findAll()) {
            if (a.getAluno().getCpf().equals(cpf)) lista.add(a);
        }
        return lista;
    }

    public List<RelatorioAmbienteDTO> relatorioPorAmbiente() {
        Map<String, String> nomesPorAmbiente = new LinkedHashMap<>();
        Map<String, Integer> quantidadePorAmbiente = new LinkedHashMap<>();
        Map<String, Long> horasPorAmbiente = new LinkedHashMap<>();

        for (Agendamento a : agendamentoRepository.findAll()) {
            String ambienteId = a.getAmbiente().getId();
            nomesPorAmbiente.put(ambienteId, a.getAmbiente().getNome());
            quantidadePorAmbiente.merge(ambienteId, 1, Integer::sum);

            long horasAgendamento = Duration.between(a.getHoraInicio(), a.getHoraFim()).toHours();
            if (horasAgendamento <= 0) horasAgendamento = 1; // mesma regra de Agendamento.calculaValorTotal()
            horasPorAmbiente.merge(ambienteId, horasAgendamento, Long::sum);
        }

        List<RelatorioAmbienteDTO> relatorio = new ArrayList<>();
        for (String ambienteId : nomesPorAmbiente.keySet()) {
            relatorio.add(new RelatorioAmbienteDTO(
                    ambienteId,
                    nomesPorAmbiente.get(ambienteId),
                    quantidadePorAmbiente.get(ambienteId),
                    horasPorAmbiente.get(ambienteId)));
        }
        return relatorio;
    }

    public RelatorioFaturamentoDTO relatorioFaturamento() {
        Map<String, Double> faturamentoPorDia = new LinkedHashMap<>();
        Map<String, Double> faturamentoPorAmbiente = new LinkedHashMap<>();
        Map<String, Double> faturamentoPorAluno = new LinkedHashMap<>();
        double faturamentoTotal = 0.0;

        for (Agendamento a : agendamentoRepository.findAll()) {
            double valor = a.getValorTotal();
            faturamentoTotal += valor;

            faturamentoPorDia.merge(a.getDataAgendamento().toString(), valor, Double::sum);
            faturamentoPorAmbiente.merge(a.getAmbiente().getNome(), valor, Double::sum);
            faturamentoPorAluno.merge(a.getAluno().getNome(), valor, Double::sum);
        }

        return new RelatorioFaturamentoDTO(faturamentoPorDia, faturamentoPorAmbiente, faturamentoPorAluno, faturamentoTotal);
    }

    public List<RelatorioServicoDTO> arrecadamentoPorServico() {
        Map<String, Integer> quantidadePorTipo = new LinkedHashMap<>();
        Map<String, Double> valorPorTipo = new LinkedHashMap<>();

        for (Agendamento a : agendamentoRepository.findAll()) {
            for (ServicoAdicional s : a.getServicosAdicionais()) {
                String tipoServico = s.getClass().getSimpleName();
                quantidadePorTipo.merge(tipoServico, 1, Integer::sum);
                valorPorTipo.merge(tipoServico, s.getValorTotal(), Double::sum);
            }
        }

        List<RelatorioServicoDTO> relatorio = new ArrayList<>();
        for (String tipoServico : quantidadePorTipo.keySet()) {
            relatorio.add(new RelatorioServicoDTO(tipoServico, quantidadePorTipo.get(tipoServico), valorPorTipo.get(tipoServico)));
        }
        return relatorio;
    }
}
