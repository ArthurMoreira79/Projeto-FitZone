package com.fitzone.api.controller;

import com.fitzone.api.dto.AgendamentoRequest;
import com.fitzone.api.dto.AgendamentoResponse;
import com.fitzone.api.dto.ServicoRequest;
import com.fitzone.domain.model.Agendamento;
import com.fitzone.domain.service.AgendamentoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    // POST /api/agendamentos -> 201 + Location
    @PostMapping
    public ResponseEntity<AgendamentoResponse> realizar(@Valid @RequestBody AgendamentoRequest request) {
        Agendamento salvo = agendamentoService.realizar(request.alunoId(), request.ambienteId(),
                request.data(), request.horaInicio(), request.horaFim());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(salvo.getId())
                .toUri();

        return ResponseEntity.created(location).body(AgendamentoResponse.from(salvo));
    }

    @GetMapping
    public ResponseEntity<List<AgendamentoResponse>> listarTodos() {
        return ResponseEntity.ok(agendamentoService.listarTodos().stream().map(AgendamentoResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoResponse> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(AgendamentoResponse.from(agendamentoService.buscarPorId(id)));
    }

    // POST /api/agendamentos/{id}/servicos -> 200 com o agendamento atualizado (novo total)
    @PostMapping("/{id}/servicos")
    public ResponseEntity<AgendamentoResponse> adicionarServico(@PathVariable Integer id,
                                                                @Valid @RequestBody ServicoRequest request) {
        return ResponseEntity.ok(AgendamentoResponse.from(agendamentoService.adicionarServico(id, request.toEntity())));
    }

    // DELETE /api/agendamentos/{id} -> 204 (cancela; os serviços vão junto)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Integer id) {
        agendamentoService.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}