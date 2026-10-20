package com.fitzone.api.controller;

import com.fitzone.api.dto.AlunoRequest;
import com.fitzone.api.dto.AlunoResponse;
import com.fitzone.domain.model.Aluno;
import com.fitzone.domain.service.AlunoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/alunos")
public class AlunoController {

    private final AlunoService alunoService;

    public AlunoController(AlunoService alunoService) {
        this.alunoService = alunoService;
    }

    // POST /api/alunos -> 201 + Location
    @PostMapping
    public ResponseEntity<AlunoResponse> cadastrar(@Valid @RequestBody AlunoRequest request) {
        Aluno salvo = alunoService.cadastrar(request.toEntity());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(salvo.getId())
                .toUri();

        return ResponseEntity.created(location).body(AlunoResponse.from(salvo));
    }

    // GET /api/alunos -> 200
    @GetMapping
    public ResponseEntity<List<AlunoResponse>> listarTodos() {
        return ResponseEntity.ok(alunoService.listarTodos().stream().map(AlunoResponse::from).toList());
    }

    // GET /api/alunos/{id} -> 200 ou 404
    @GetMapping("/{id}")
    public ResponseEntity<AlunoResponse> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(AlunoResponse.from(alunoService.buscarPorId(id)));
    }

    // GET /api/alunos/busca?cpf=12345678901 -> 200 ou 404
    @GetMapping("/busca")
    public ResponseEntity<AlunoResponse> buscarPorCpf(@RequestParam String cpf) {
        return ResponseEntity.ok(AlunoResponse.from(alunoService.buscarPorCpf(cpf)));
    }

    // PUT /api/alunos/{id} -> 200, 400, 404 ou 409
    @PutMapping("/{id}")
    public ResponseEntity<AlunoResponse> atualizar(@PathVariable Integer id, @Valid @RequestBody AlunoRequest request) {
        return ResponseEntity.ok(AlunoResponse.from(alunoService.atualizar(id, request.toEntity())));
    }

    // DELETE /api/alunos/{id} -> 204, 404 ou 409 (aluno com agendamentos)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        alunoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}