package controle;

import entidades.*;
import excecoes.*;
import org.junit.jupiter.api.*;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class AdministradorSistemaTest {

    private AdministradorSistema admin;

    /**
     * Roda antes de CADA @Test, recriando o AdministradorSistema do zero.
     * Isso evita que um teste "contamine" o próximo com dados que ele mesmo cadastrou.
     */
    @BeforeEach
    void setUp() throws Exception {
        admin = new AdministradorSistema();
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

}
