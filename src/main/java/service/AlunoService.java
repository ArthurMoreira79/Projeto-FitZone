package service;

import entidades.Aluno;
import excecoes.AlunoJaCadastradoException;
import excecoes.AlunoNaoEncontradoException;
import org.springframework.stereotype.Service;
import repository.AlunoRepository;

import java.util.List;

@Service
public class AlunoService {

    private final AlunoRepository alunoRepository;

    public AlunoService(AlunoRepository alunoRepository) {
        this.alunoRepository = alunoRepository;
    }

    public Aluno cadastrarAluno(Aluno aluno) throws AlunoJaCadastradoException {
        if (alunoRepository.existsById(aluno.getCpf())) {
            throw new AlunoJaCadastradoException("CPF já existe.");
        }
        return alunoRepository.save(aluno);
    }

    public Aluno buscarAluno(String cpf) throws AlunoNaoEncontradoException {
        return alunoRepository.findById(cpf)
                .orElseThrow(() -> new AlunoNaoEncontradoException("Aluno não encontrado."));
    }

    public List<Aluno> listarAlunos() {
        return alunoRepository.findAll();
    }
}
