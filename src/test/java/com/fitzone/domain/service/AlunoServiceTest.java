package com.fitzone.domain.service;

import com.fitzone.domain.exception.AlunoJaCadastradoException;
import com.fitzone.domain.exception.AlunoNaoEncontradoException;
import com.fitzone.domain.model.Aluno;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional // rollback ao fim de cada teste: o banco volta vazio
class AlunoServiceTest {

    @Autowired
    private AlunoService alunoService;

    private Aluno novoAluno(String cpf, String email) {
        return new Aluno(cpf, "Aluno Teste", email, "11987654321", null);
    }

    @Test
    void deveCadastrarAlunoComIdEDataDeCadastroDeHoje() {
        Aluno salvo = alunoService.cadastrar(novoAluno("12345678901", "ana@email.com"));

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getDataCadastro()).isEqualTo(LocalDate.now());
    }

    @Test
    void naoDeveCadastrarCpfDuplicado() {
        alunoService.cadastrar(novoAluno("12345678901", "ana@email.com"));

        assertThatThrownBy(() -> alunoService.cadastrar(novoAluno("12345678901", "outro@email.com")))
                .isInstanceOf(AlunoJaCadastradoException.class)
                .hasMessageContaining("CPF");
    }

    @Test
    void naoDeveCadastrarEmailDuplicado() {
        alunoService.cadastrar(novoAluno("12345678901", "ana@email.com"));

        assertThatThrownBy(() -> alunoService.cadastrar(novoAluno("98765432100", "ana@email.com")))
                .isInstanceOf(AlunoJaCadastradoException.class)
                .hasMessageContaining("E-mail");
    }

    @Test
    void devePreservarDataDeCadastroAoAtualizar() {
        Aluno salvo = alunoService.cadastrar(novoAluno("12345678901", "ana@email.com"));
        LocalDate dataOriginal = salvo.getDataCadastro();

        // os dados novos vêm sem data de cadastro, como chegariam do AlunoRequest
        Aluno dados = new Aluno("12345678901", "Nome Atualizado", "ana@email.com", "11987654321", null);
        Aluno atualizado = alunoService.atualizar(salvo.getId(), dados);

        assertThat(atualizado.getNome()).isEqualTo("Nome Atualizado");
        assertThat(atualizado.getDataCadastro()).isEqualTo(dataOriginal);
    }

    @Test
    void naoDeveAtualizarParaCpfDeOutroAluno() {
        alunoService.cadastrar(novoAluno("12345678901", "ana@email.com"));
        Aluno bruno = alunoService.cadastrar(novoAluno("98765432100", "bruno@email.com"));

        Aluno dados = novoAluno("12345678901", "bruno@email.com");

        assertThatThrownBy(() -> alunoService.atualizar(bruno.getId(), dados))
                .isInstanceOf(AlunoJaCadastradoException.class);
    }

    @Test
    void deveLancarExcecaoParaAlunoInexistente() {
        assertThatThrownBy(() -> alunoService.buscarPorId(999999))
                .isInstanceOf(AlunoNaoEncontradoException.class);
    }
}