package service;

import app.FitzoneApplication;
import dto.AgendamentoRequest;
import dto.ServicoRequest;
import entidades.Agendamento;
import entidades.Aluno;
import entidades.Ambiente;
import entidades.TipoAmbiente;
import excecoes.AgendamentoNaoEncontradoException;
import excecoes.AmbienteIndisponivelException;
import excecoes.ServicoInvalidoException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import repository.AgendamentoRepository;
import repository.AlunoRepository;
import repository.AmbienteRepository;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = FitzoneApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AgendamentoServiceTest {

    @Autowired
    private AlunoService alunoService;
    @Autowired
    private AmbienteService ambienteService;
    @Autowired
    private AgendamentoService agendamentoService;
    @Autowired
    private AlunoRepository alunoRepository;
    @Autowired
    private AmbienteRepository ambienteRepository;
    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @BeforeEach
    void setUp() {
        limparBanco();
    }

    @AfterAll
    void tearDown() {
        limparBanco();
    }

    private void limparBanco() {
        agendamentoRepository.deleteAllInBatch();
        ambienteRepository.deleteAllInBatch();
        alunoRepository.deleteAllInBatch();
    }

    @Test
    void deveLancarExcecaoAoAgendarHorarioSobreposto() throws Exception {
        Aluno aluno = alunoService.cadastrarAluno(new Aluno("11111111111", "Teste", "a@a.com", "123", LocalDate.now()));
        Ambiente ambiente = ambienteService.cadastrarAmbiente(TipoAmbiente.MUSCULACAO, "Sala Musc 1");

        // Primeira reserva: 08:00-09:00, deve ser aceita normalmente.
        agendamentoService.realizarAgendamento(new AgendamentoRequest(
                aluno.getCpf(), ambiente.getId(), LocalDate.now().plusDays(1), LocalTime.of(8, 0), LocalTime.of(9, 0)));

        // Segunda reserva no mesmo ambiente/data, com horário que se sobrepõe (08:30-09:30).
        AgendamentoRequest segunda = new AgendamentoRequest(
                aluno.getCpf(), ambiente.getId(), LocalDate.now().plusDays(1), LocalTime.of(8, 30), LocalTime.of(9, 30));

        assertThrows(AmbienteIndisponivelException.class, () -> agendamentoService.realizarAgendamento(segunda));
    }

    @Test
    void deveLancarExcecaoAoCancelarAgendamentoInexistente() {
        assertThrows(AgendamentoNaoEncontradoException.class, () -> agendamentoService.cancelarAgendamento(9999));
    }

    @Test
    void deveLancarExcecaoAoAdicionarServicoInvalidoAAgendamentoExistente() throws Exception {
        Aluno aluno = alunoService.cadastrarAluno(new Aluno("11111111111", "Teste", "a@a.com", "123", LocalDate.now()));
        Ambiente ambiente = ambienteService.cadastrarAmbiente(TipoAmbiente.MUSCULACAO, "Sala Musc 1");

        Agendamento agendamento = agendamentoService.realizarAgendamento(new AgendamentoRequest(
                aluno.getCpf(), ambiente.getId(), LocalDate.now().plusDays(1), LocalTime.of(8, 0), LocalTime.of(9, 0)));

        // AgendamentoNaoEncontradoException para ID inexistente:
        assertThrows(AgendamentoNaoEncontradoException.class,
                () -> agendamentoService.adicionarServicoAoAgendamento(9999, new ServicoRequest("PERSONAL_TRAINER", null)));

        // ServicoInvalidoException para tipo nulo num agendamento que existe:
        assertThrows(ServicoInvalidoException.class,
                () -> agendamentoService.adicionarServicoAoAgendamento(agendamento.getId(), new ServicoRequest(null, null)));
    }

    @Test
    void deveCalcularValorTotalComServicosAdicionais() throws Exception {
        Aluno aluno = alunoService.cadastrarAluno(new Aluno("11111111111", "Teste", "a@a.com", "123", LocalDate.now()));
        // SalaMusculacao tem valor fixo de R$100,00/hora.
        Ambiente ambiente = ambienteService.cadastrarAmbiente(TipoAmbiente.MUSCULACAO, "Sala Musc 1");

        // 1 hora de reserva (08:00-09:00) = R$100,00 de base.
        Agendamento agendamento = agendamentoService.realizarAgendamento(new AgendamentoRequest(
                aluno.getCpf(), ambiente.getId(), LocalDate.now().plusDays(1), LocalTime.of(8, 0), LocalTime.of(9, 0)));

        // + Personal Trainer (R$50,00) + Locker com 2 unidades (R$5,00 cada = R$10,00)
        agendamentoService.adicionarServicoAoAgendamento(agendamento.getId(), new ServicoRequest("PERSONAL_TRAINER", null));
        agendamentoService.adicionarServicoAoAgendamento(agendamento.getId(), new ServicoRequest("LOCKER", 2));

        Agendamento atualizado = agendamentoService.buscarAgendamento(agendamento.getId());

        // Total esperado: 100 (ambiente) + 50 (personal trainer) + 10 (2 lockers) = 160
        assertEquals(160.0, atualizado.getValorTotal());
    }
}
