package service;

import app.FitzoneApplication;
import entidades.Aluno;
import excecoes.AlunoJaCadastradoException;
import excecoes.AlunoNaoEncontradoException;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Fase 4: testes de integração contra o MySQL real (schema "fitzone"), assim
 * como na Fase 2 — a diferença é que agora testam o @Service diretamente
 * (injetado pelo contexto Spring) em vez do antigo AdministradorSistema.
 */
@SpringBootTest(classes = FitzoneApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AlunoServiceTest {

    @Autowired
    private AlunoService alunoService;
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
    void deveCadastrarAlunoComSucesso() throws Exception {
        Aluno aluno = new Aluno("11111111111", "Teste", "a@a.com", "123", LocalDate.now());

        alunoService.cadastrarAluno(aluno);

        Aluno encontrado = alunoService.buscarAluno("11111111111");
        assertEquals("Teste", encontrado.getNome());
    }

    @Test
    void deveLancarExcecaoAoCadastrarAlunoComCpfDuplicado() throws Exception {
        Aluno aluno = new Aluno("11111111111", "Teste", "a@a.com", "123", LocalDate.now());
        alunoService.cadastrarAluno(aluno);

        assertThrows(AlunoJaCadastradoException.class, () -> alunoService.cadastrarAluno(aluno));
    }

    @Test
    void deveLancarExcecaoAoBuscarAlunoInexistente() {
        assertThrows(AlunoNaoEncontradoException.class, () -> alunoService.buscarAluno("00000000000"));
    }
}
