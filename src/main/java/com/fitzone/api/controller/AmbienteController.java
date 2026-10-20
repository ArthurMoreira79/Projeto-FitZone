package com.fitzone.api.controller;

import com.fitzone.api.dto.AmbienteObservacoesRequest;
import com.fitzone.api.dto.AmbienteRequest;
import com.fitzone.api.dto.AmbienteResponse;
import com.fitzone.domain.model.Ambiente;
import com.fitzone.domain.service.AmbienteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/ambientes")
public class AmbienteController {

    private final AmbienteService ambienteService;

    public AmbienteController(AmbienteService ambienteService) {
        this.ambienteService = ambienteService;
    }

    // POST /api/ambientes -> 201 + Location (id gerado pela faixa do tipo)
    @PostMapping
    public ResponseEntity<AmbienteResponse> cadastrar(@Valid @RequestBody AmbienteRequest request) {
        Ambiente salvo = ambienteService.cadastrar(request.tipo(), request.observacoes());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(salvo.getId())
                .toUri();

        return ResponseEntity.created(location).body(AmbienteResponse.from(salvo));
    }

    @GetMapping
    public ResponseEntity<List<AmbienteResponse>> listarTodos() {
        return ResponseEntity.ok(ambienteService.listarTodos().stream().map(AmbienteResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AmbienteResponse> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(AmbienteResponse.from(ambienteService.buscarPorId(id)));
    }

    // PUT /api/ambientes/{id} -> só as observações são editáveis
    @PutMapping("/{id}")
    public ResponseEntity<AmbienteResponse> atualizarObservacoes(@PathVariable Integer id,
                                                                 @Valid @RequestBody AmbienteObservacoesRequest request) {
        return ResponseEntity.ok(AmbienteResponse.from(ambienteService.atualizarObservacoes(id, request.observacoes())));
    }

    // DELETE /api/ambientes/{id} -> 204, 404 ou 409 (ambiente com agendamentos)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        ambienteService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}