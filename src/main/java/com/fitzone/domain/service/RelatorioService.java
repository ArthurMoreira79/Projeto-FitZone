package com.fitzone.domain.service;

import com.fitzone.api.dto.*;
import com.fitzone.domain.model.Agendamento;
import com.fitzone.domain.model.Aluno;
import com.fitzone.domain.model.ServicoAdicional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class RelatorioService {

    private final AgendamentoService agendamentoService;
    private final AlunoService alunoService;
    private final AmbienteService ambienteService;

    public RelatorioService(AgendamentoService agendamentoService,
                            AlunoService alunoService,
                            AmbienteService ambienteService) {
        this.agendamentoService = agendamentoService;
        this.alunoService = alunoService;
        this.ambienteService = ambienteService;
    }

    /**
     * Relatório 1 - agendamentos de um aluno, com totais de horas e valor.
     */
    @Transactional(readOnly = true)
    public RelatorioAlunoDTO porAluno(Integer alunoId) {
        Aluno aluno = alunoService.buscarPorId(alunoId); // 404 se não existir
        List<Agendamento> agendamentos = agendamentoService.listarPorAluno(alunoId);

        long horas = agendamentos.stream().mapToLong(Agendamento::getHorasCobradas).sum();
        double valor = agendamentos.stream().mapToDouble(Agendamento::getValorTotal).sum();

        return new RelatorioAlunoDTO(aluno.getId(), aluno.getNome(), agendamentos.size(), horas, valor,
                agendamentos.stream().map(AgendamentoResponse::from).toList());
    }

    /**
     * Relatório 2 - ocupação: quantidade de agendamentos e horas cobradas por ambiente.
     * Inclui ambientes sem nenhum agendamento (ocupação zero também é informação).
     */
    @Transactional(readOnly = true)
    public List<RelatorioAmbienteDTO> porAmbiente() {
        Map<Integer, List<Agendamento>> agendamentosPorAmbiente = agendamentoService.listarTodos().stream()
                .collect(Collectors.groupingBy(a -> a.getAmbiente().getId()));

        return ambienteService.listarTodos().stream()
                .map(ambiente -> {
                    List<Agendamento> doAmbiente = agendamentosPorAmbiente.getOrDefault(ambiente.getId(), List.of());
                    // mesma regra de duração do valor cobrado: horas completas, mínimo de 1h
                    long horas = doAmbiente.stream().mapToLong(Agendamento::getHorasCobradas).sum();
                    return new RelatorioAmbienteDTO(ambiente.getId(), ambiente.getNome(), ambiente.getTipo(),
                            doAmbiente.size(), horas);
                })
                .sorted(Comparator.comparing(RelatorioAmbienteDTO::ambienteId))
                .toList();
    }

    /**
     * Relatório 3 - faturamento consolidado por dia, por ambiente e por aluno, com total geral.
     */
    @Transactional(readOnly = true)
    public RelatorioFaturamentoDTO faturamento() {
        List<Agendamento> todos = agendamentoService.listarTodos();

        Map<LocalDate, Double> porDia = todos.stream()
                .collect(Collectors.groupingBy(Agendamento::getDataAgendamento, TreeMap::new,
                        Collectors.summingDouble(Agendamento::getValorTotal)));

        Map<String, Double> porAmbiente = todos.stream()
                .collect(Collectors.groupingBy(a -> a.getAmbiente().getNome(), TreeMap::new,
                        Collectors.summingDouble(Agendamento::getValorTotal)));

        // nome + id na chave: dois alunos com o mesmo nome não podem ter o faturamento somado junto
        Map<String, Double> porAluno = todos.stream()
                .collect(Collectors.groupingBy(a -> a.getAluno().getNome() + " (id " + a.getAluno().getId() + ")",
                        TreeMap::new, Collectors.summingDouble(Agendamento::getValorTotal)));

        double total = todos.stream().mapToDouble(Agendamento::getValorTotal).sum();

        return new RelatorioFaturamentoDTO(porDia, porAmbiente, porAluno, total);
    }

    /**
     * Relatório 4 - quantidade contratada e valor arrecadado por tipo de serviço adicional.
     */
    @Transactional(readOnly = true)
    public List<RelatorioServicoDTO> porServico() {
        // flatMap "achata" a lista de listas: de cada agendamento, tira os seus serviços
        Map<String, List<ServicoAdicional>> servicosPorTipo = agendamentoService.listarTodos().stream()
                .flatMap(a -> a.getServicosAdicionais().stream())
                .collect(Collectors.groupingBy(s -> s.getClass().getSimpleName(), TreeMap::new, Collectors.toList()));

        return servicosPorTipo.entrySet().stream()
                .map(grupo -> new RelatorioServicoDTO(
                        grupo.getKey(),
                        grupo.getValue().size(),
                        grupo.getValue().stream().mapToDouble(ServicoAdicional::getValorTotal).sum()))
                .toList();
    }
}