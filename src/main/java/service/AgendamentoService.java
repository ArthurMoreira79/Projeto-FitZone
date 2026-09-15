package service;

import dto.AgendamentoRequest;
import dto.ServicoRequest;
import entidades.Agendamento;
import entidades.Aluno;
import entidades.Ambiente;
import entidades.AvaliacaoFisica;
import entidades.LockerAcademia;
import entidades.Nutricionista;
import entidades.PersonalTrainer;
import entidades.ServicoAdicional;
import excecoes.AgendamentoNaoEncontradoException;
import excecoes.AlunoNaoEncontradoException;
import excecoes.AmbienteIndisponivelException;
import excecoes.AmbienteNaoEncontradoException;
import excecoes.ServicoInvalidoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.AgendamentoRepository;

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
    public Agendamento realizarAgendamento(AgendamentoRequest req)
            throws AlunoNaoEncontradoException, AmbienteNaoEncontradoException, AmbienteIndisponivelException {
        Aluno aluno = alunoService.buscarAluno(req.alunoCpf());
        Ambiente ambiente = ambienteService.buscarAmbiente(req.ambienteId());

        List<Agendamento> conflitos = agendamentoRepository.buscarConflitos(
                ambiente.getId(), req.dataAgendamento(), req.horaInicio(), req.horaFim());
        if (!conflitos.isEmpty()) {
            Agendamento c = conflitos.get(0);
            throw new AmbienteIndisponivelException("Ambiente já reservado das " + c.getHoraInicio() + " às " + c.getHoraFim());
        }

        Agendamento novo = new Agendamento(aluno, ambiente, req.dataAgendamento(), req.horaInicio(), req.horaFim());
        return agendamentoRepository.save(novo);
    }

    public Agendamento buscarAgendamento(int id) throws AgendamentoNaoEncontradoException {
        return agendamentoRepository.findById(id)
                .orElseThrow(() -> new AgendamentoNaoEncontradoException("Agendamento não encontrado."));
    }

    @Transactional
    public void cancelarAgendamento(int id) throws AgendamentoNaoEncontradoException {
        Agendamento a = buscarAgendamento(id);
        agendamentoRepository.delete(a);
    }

    public List<Agendamento> listarAgendamentos() {
        return agendamentoRepository.findAll();
    }

    @Transactional
    public Agendamento adicionarServicoAoAgendamento(int idAgendamento, ServicoRequest req)
            throws AgendamentoNaoEncontradoException, ServicoInvalidoException {
        Agendamento agendamento = buscarAgendamento(idAgendamento);
        ServicoAdicional servico = construirServico(req);
        agendamento.adicionarServico(servico);
        return agendamentoRepository.save(agendamento);
    }

    private ServicoAdicional construirServico(ServicoRequest req) throws ServicoInvalidoException {
        if (req == null || req.tipo() == null) {
            throw new ServicoInvalidoException("Serviço adicional não pode ser nulo.");
        }
        return switch (req.tipo()) {
            case "PERSONAL_TRAINER" -> new PersonalTrainer();
            case "NUTRICIONISTA" -> new Nutricionista();
            case "AVALIACAO_FISICA" -> new AvaliacaoFisica();
            case "LOCKER" -> new LockerAcademia(req.quantidade() != null ? req.quantidade() : 0);
            default -> throw new ServicoInvalidoException("Tipo de serviço inválido: " + req.tipo());
        };
    }
}
