package com.fitzone.domain.service;

import com.fitzone.domain.exception.AlunoJaCadastradoException;
import com.fitzone.domain.exception.AlunoNaoEncontradoException;
import com.fitzone.domain.model.Aluno;
import com.fitzone.domain.repository.AlunoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AlunoService {

    private final AlunoRepository alunoRepository;

    public AlunoService(AlunoRepository alunoRepository) {
        this.alunoRepository = alunoRepository;
    }

    @Transactional
    public Aluno cadastrar(Aluno aluno) {
        if (alunoRepository.existsByCpf(aluno.getCpf())) {
            throw new AlunoJaCadastradoException("CPF " + aluno.getCpf() + " já cadastrado.");
        }
        if (alunoRepository.existsByEmail(aluno.getEmail())) {
            throw new AlunoJaCadastradoException("E-mail " + aluno.getEmail() + " já cadastrado.");
        }
        // garante INSERT: se viesse um id, o save() atualizaria outro aluno
        aluno.setId(null);
        // a data de cadastro é regra do sistema, não um dado que o cliente informa
        aluno.setDataCadastro(LocalDate.now());
        return alunoRepository.save(aluno);
    }

    @Transactional(readOnly = true)
    public List<Aluno> listarTodos() {
        return alunoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Aluno buscarPorId(Integer id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> new AlunoNaoEncontradoException("Aluno de id " + id + " não encontrado."));
    }

    @Transactional(readOnly = true)
    public Aluno buscarPorCpf(String cpf) {
        return alunoRepository.findByCpf(cpf)
                .orElseThrow(() -> new AlunoNaoEncontradoException("Aluno de CPF " + cpf + " não encontrado."));
    }

    @Transactional
    public Aluno atualizar(Integer id, Aluno dados) {
        Aluno atual = buscarPorId(id); // 404 se não existir

        if (alunoRepository.existsByCpfAndIdNot(dados.getCpf(), id)) {
            throw new AlunoJaCadastradoException("CPF " + dados.getCpf() + " já pertence a outro aluno.");
        }
        if (alunoRepository.existsByEmailAndIdNot(dados.getEmail(), id)) {
            throw new AlunoJaCadastradoException("E-mail " + dados.getEmail() + " já pertence a outro aluno.");
        }

        // Copia só os campos editáveis para a entidade gerenciada; o dirty checking
        // faz o UPDATE no commit. A data de cadastro original é preservada.
        atual.setCpf(dados.getCpf());
        atual.setNome(dados.getNome());
        atual.setEmail(dados.getEmail());
        atual.setTelefone(dados.getTelefone());
        return atual;
    }

    @Transactional
    public void excluir(Integer id) {
        Aluno aluno = buscarPorId(id);
        alunoRepository.delete(aluno);
        // Força o DELETE agora: se o aluno tiver agendamentos, o ON DELETE RESTRICT (V3)
        // falha aqui dentro e vira DataIntegrityViolationException (409 no handler)
        alunoRepository.flush();
    }
}