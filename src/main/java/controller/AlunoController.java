package controller;

import dto.AlunoRequest;
import dto.AlunoResponse;
import excecoes.AlunoJaCadastradoException;
import excecoes.AlunoNaoEncontradoException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import service.AlunoService;

import java.util.List;

@RestController
@RequestMapping("/api/alunos")
public class AlunoController {

    private final AlunoService alunoService;

    public AlunoController(AlunoService alunoService) {
        this.alunoService = alunoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlunoResponse cadastrar(@Valid @RequestBody AlunoRequest req) throws AlunoJaCadastradoException {
        return AlunoResponse.from(alunoService.cadastrarAluno(req.toEntity()));
    }

    @GetMapping
    public List<AlunoResponse> listar() {
        return alunoService.listarAlunos().stream().map(AlunoResponse::from).toList();
    }

    @GetMapping("/{cpf}")
    public AlunoResponse buscar(@PathVariable String cpf) throws AlunoNaoEncontradoException {
        return AlunoResponse.from(alunoService.buscarAluno(cpf));
    }
}
