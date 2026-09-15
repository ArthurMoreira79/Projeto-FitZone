package service;

import app.FitzoneApplication;
import entidades.TipoAmbiente;
import excecoes.LimiteAmbienteExcedidoException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import repository.AgendamentoRepository;
import repository.AlunoRepository;
import repository.AmbienteRepository;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = FitzoneApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AmbienteServiceTest {

    @Autowired
    private AmbienteService ambienteService;
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
    void deveLancarExcecaoAoEsgotarFaixaDeIdsDoTipo() throws Exception {
        // Faixa de SalaMusculacao é 101-120 (20 vagas) - cadastra todas.
        for (int i = 0; i < 20; i++) {
            ambienteService.cadastrarAmbiente(TipoAmbiente.MUSCULACAO, null);
        }

        // A 21ª deve estourar o limite da faixa.
        assertThrows(LimiteAmbienteExcedidoException.class,
                () -> ambienteService.cadastrarAmbiente(TipoAmbiente.MUSCULACAO, null));
    }
}
