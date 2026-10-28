package com.fitzone.domain.service;

import com.fitzone.domain.exception.AgendamentoNaoEncontradoException;
import com.fitzone.domain.exception.AmbienteIndisponivelException;
import com.fitzone.domain.exception.HorarioInvalidoException;
import com.fitzone.domain.exception.ServicoInvalidoException;
import com.fitzone.domain.model.Agendamento;
import com.fitzone.domain.model.Aluno;
import com.fitzone.domain.model.Ambiente;
import com.fitzone.domain.model.ServicoAdicional;
import com.fitzone.domain.repository.AgendamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final AlunoService alunoService;
    private final AmbienteService ambienteService;

    public AgendamentoService(AgendamentoRepository agendamentoRepository,
                              AlunoService alunoService,
                              AmbienteService ambienteService) {
        this.agendamentoRepository = agendamentoRepository;
        this.alunoService = alunoService;
        this.ambienteService = ambienteService;
    }

    @Transactional
    public Agendamento realizar(Integer alunoId, Integer ambienteId, LocalDate data,
                                LocalTime horaInicio, LocalTime horaFim) {
        validarHorario(horaInicio, horaFim);

        // reaproveita os buscarPorId: 404 automático se aluno ou ambiente não existirem
        Aluno aluno = alunoService.buscarPorId(alunoId);
        Ambiente ambiente = ambienteService.buscarPorId(ambienteId);

        // conflito checado no banco (consulta + índice), não em memória
        List<Agendamento> conflitos = agendamentoRepository.buscarConflitos(ambienteId, data, horaInicio, horaFim);
        if (!conflitos.isEmpty()) {
            Agendamento existente = conflitos.get(0);
            throw new AmbienteIndisponivelException("Ambiente já reservado das "
                    + existente.getHoraInicio() + " às " + existente.getHoraFim() + ".");
        }

        // o construtor já calcula o valor total (horas cobradas × valor/hora)
        return agendamentoRepository.save(new Agendamento(aluno, ambiente, data, horaInicio, horaFim));
    }

    /**
     * Prévia do valor de uma reserva, sem gravar nada. Monta um Agendamento só em memória
     * (nunca passa pelo save) para reaproveitar getHorasCobradas() e o cálculo do valor:
     * a regra de cobrança continua existindo em um único lugar.
     */
    @Transactional(readOnly = true)
    public Agendamento orcar(Integer ambienteId, LocalTime horaInicio, LocalTime horaFim) {
        validarHorario(horaInicio, horaFim);
        Ambiente ambiente = ambienteService.buscarPorId(ambienteId);
        return new Agendamento(null, ambiente, null, horaInicio, horaFim);
    }

    private void validarHorario(LocalTime horaInicio, LocalTime horaFim) {
        if (!horaFim.isAfter(horaInicio)) {
            throw new HorarioInvalidoException("A hora de fim (" + horaFim
                    + ") deve ser posterior à hora de início (" + horaInicio + ").");
        }
    }

    @Transactional(readOnly = true)
    public List<Agendamento> listarTodos() {
        return agendamentoRepository.findAll();
    }

    /** Filtros opcionais (null = sem filtro), em ordem de data e hora. */
    @Transactional(readOnly = true)
    public List<Agendamento> listar(LocalDate data, Integer ambienteId, Integer alunoId) {
        return agendamentoRepository.buscarComFiltros(data, ambienteId, alunoId);
    }

    @Transactional(readOnly = true)
    public Agendamento buscarPorId(Integer id) {
        return agendamentoRepository.findById(id)
                .orElseThrow(() -> new AgendamentoNaoEncontradoException("Agendamento de id " + id + " não encontrado."));
    }

    @Transactional(readOnly = true)
    public List<Agendamento> listarPorAluno(Integer alunoId) {
        alunoService.buscarPorId(alunoId); // 404 se o aluno não existir, em vez de lista vazia
        return agendamentoRepository.findByAluno_IdOrderByDataAgendamentoAscHoraInicioAsc(alunoId);
    }

    @Transactional
    public Agendamento adicionarServico(Integer agendamentoId, ServicoAdicional servico) {
        if (servico == null) {
            throw new ServicoInvalidoException("Serviço adicional não pode ser nulo.");
        }
        Agendamento agendamento = buscarPorId(agendamentoId);

        // vincula o serviço e recalcula o total; o cascade = ALL do @OneToMany
        // grava o serviço novo e o dirty checking atualiza o valor_total no commit
        agendamento.adicionarServico(servico);
        return agendamento;
    }

    @Transactional
    public void cancelar(Integer id) {
        Agendamento agendamento = buscarPorId(id);
        // os serviços adicionais vão junto (orphanRemoval + ON DELETE CASCADE do V4)
        agendamentoRepository.delete(agendamento);
    }
}