package controle;

import entidades.*;
import excecoes.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Fase 2: estes testes agora rodam contra o banco MySQL real (schema "fitzone"),
 * então são testes de integração, não mais testes puramente unitários com
 * arquivos locais. Por isso o @BeforeEach limpa as tabelas diretamente via
 * EntityManager antes de cada teste, no lugar de apagar os antigos .dat.
 *
 * A ordem do DELETE respeita as chaves estrangeiras: primeiro os
 * servicos_adicionais e agendamentos, depois ambientes e alunos.
 */
public class AdministradorSistemaTest {

    private AdministradorSistema admin;

    @BeforeEach
    void setUp() throws Exception {
        limparBanco();
        admin = new AdministradorSistema();
    }

    private void limparBanco() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM ServicoAdicional").executeUpdate();
            em.createQuery("DELETE FROM Agendamento").executeUpdate();
            em.createQuery("DELETE FROM Ambiente").executeUpdate();
            em.createQuery("DELETE FROM Aluno").executeUpdate();
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @Test
    void deveCadastrarAlunoComSucesso() throws Exception {
        Aluno aluno = new Aluno("11111111111", "Teste", "a@a.com", "123", LocalDate.now());

        admin.cadastrarAluno(aluno);

        /** Se buscarAluno não lançar exceção e devolver o mesmo cpf, o cadastro funcionou */
        Aluno encontrado = admin.buscarAluno("11111111111");
        assertEquals("Teste", encontrado.getNome());
    }

    @Test
    void deveLancarExcecaoAoCadastrarAlunoComCpfDuplicado() throws Exception {
        Aluno aluno = new Aluno("11111111111", "Teste", "a@a.com", "123", LocalDate.now());
        admin.cadastrarAluno(aluno);
 
        /**
         *  assertThrows executa o lambda e verifica se ele lança exatamente essa exceção.
         * Se não lançar nenhuma, ou lançar uma diferente, o teste falha.
        */
        assertThrows(AlunoJaCadastradoException.class, () -> admin.cadastrarAluno(aluno));
    }
 
    @Test
    void deveLancarExcecaoAoBuscarAlunoInexistente() {
        assertThrows(AlunoNaoEncontradoException.class, () -> admin.buscarAluno("00000000000"));
    }
 
    @Test
    void deveLancarExcecaoAoCadastrarAmbienteComIdDuplicado() throws Exception {
        admin.cadastrarAmbiente(new SalaMusculacao("A1", "Sala Musc 1"));
 
        assertThrows(AmbienteJaCadastradoException.class,
                () -> admin.cadastrarAmbiente(new SalaMusculacao("A1", "Sala Musc 2")));
    }

    @Test
    void deveLancarExcecaoAoAgendarHorarioSobreposto() throws Exception {
        Aluno aluno = new Aluno("11111111111", "Teste", "a@a.com", "123", LocalDate.now());
        admin.cadastrarAluno(aluno);
        Ambiente ambiente = new SalaMusculacao("A1", "Sala Musc 1");
        admin.cadastrarAmbiente(ambiente);

        // Primeira reserva: 08:00-09:00, deve ser aceita normalmente.
        Agendamento primeira = new Agendamento(aluno, ambiente,
                LocalDate.now().plusDays(1), LocalTime.of(8, 0), LocalTime.of(9, 0));
        admin.realizarAgendamento(primeira);

        // Segunda reserva no mesmo ambiente/data, com horário que se sobrepõe (08:30-09:30).
        Agendamento segunda = new Agendamento(aluno, ambiente,
                LocalDate.now().plusDays(1), LocalTime.of(8, 30), LocalTime.of(9, 30));

        assertThrows(AmbienteIndisponivelException.class, () -> admin.realizarAgendamento(segunda));
    }

    @Test
    void deveLancarExcecaoAoCancelarAgendamentoInexistente() {
        assertThrows(AgendamentoNaoEncontradoException.class, () -> admin.cancelarAgendamento(9999));
    }

    @Test
    void deveLancarExcecaoAoAdicionarServicoNuloAAgendamentoExistente() throws Exception {
        Aluno aluno = new Aluno("11111111111", "Teste", "a@a.com", "123", LocalDate.now());
        admin.cadastrarAluno(aluno);
        Ambiente ambiente = new SalaMusculacao("A1", "Sala Musc 1");
        admin.cadastrarAmbiente(ambiente);

        Agendamento agendamento = new Agendamento(aluno, ambiente,
                LocalDate.now().plusDays(1), LocalTime.of(8, 0), LocalTime.of(9, 0));
        admin.realizarAgendamento(agendamento);

        // AgendamentoNaoEncontradoException para ID inexistente:
        assertThrows(AgendamentoNaoEncontradoException.class,
                () -> admin.adicionarServicoAoAgendamento(9999, new PersonalTrainer()));

        // ServicoInvalidoException para serviço nulo num agendamento que existe:
        assertThrows(ServicoInvalidoException.class,
                () -> admin.adicionarServicoAoAgendamento(agendamento.getId(), null));
    }

    @Test
    void deveCalcularValorTotalComServicosAdicionais() throws Exception {
        Aluno aluno = new Aluno("11111111111", "Teste", "a@a.com", "123", LocalDate.now());
        admin.cadastrarAluno(aluno);
        // SalaMusculacao tem valor fixo de R$100,00/hora.
        Ambiente ambiente = new SalaMusculacao("A1", "Sala Musc 1");
        admin.cadastrarAmbiente(ambiente);

        // 1 hora de reserva (08:00-09:00) = R$100,00 de base.
        Agendamento agendamento = new Agendamento(aluno, ambiente,
                LocalDate.now().plusDays(1), LocalTime.of(8, 0), LocalTime.of(9, 0));
        admin.realizarAgendamento(agendamento);

        // + Personal Trainer (R$50,00) + Locker com 2 unidades (R$5,00 cada = R$10,00)
        admin.adicionarServicoAoAgendamento(agendamento.getId(), new PersonalTrainer());
        admin.adicionarServicoAoAgendamento(agendamento.getId(), new LockerAcademia(2));

        // Fase 2: cada chamada de repositório abre seu próprio EntityManager, então
        // "agendamento" (referência local) não é mais o mesmo objeto atualizado
        // internamente pelo AdministradorSistema - precisamos buscar de novo.
        Agendamento atualizado = admin.listarAgendamentos().stream()
                .filter(a -> a.getId() == agendamento.getId())
                .findFirst()
                .orElseThrow();

        // Total esperado: 100 (ambiente) + 50 (personal trainer) + 10 (2 lockers) = 160
        assertEquals(160.0, atualizado.getValorTotal());
    }
}