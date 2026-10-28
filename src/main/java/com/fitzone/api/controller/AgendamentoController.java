package com.fitzone.api.controller;

import com.fitzone.api.dto.AgendamentoRequest;
import com.fitzone.api.dto.AgendamentoResponse;
import com.fitzone.api.dto.OrcamentoResponse;
import com.fitzone.api.dto.ServicoRequest;
import com.fitzone.domain.model.Agendamento;
import com.fitzone.domain.service.AgendamentoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
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

    // GET /api/agendamentos?data=2026-12-01&ambienteId=101&alunoId=5 -> 200 (todos os filtros são opcionais)
    // @DateTimeFormat(iso = DATE): sem ele, o Spring tentaria ler a data no formato do idioma do servidor
    @GetMapping
    public ResponseEntity<List<AgendamentoResponse>> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(required = false) Integer ambienteId,
            @RequestParam(required = false) Integer alunoId) {
        return ResponseEntity.ok(agendamentoService.listar(data, ambienteId, alunoId).stream()
                .map(AgendamentoResponse::from).toList());
    }

    // GET /api/agendamentos/orcamento?ambienteId=101&horaInicio=08:00&horaFim=10:40 -> 200 (nada é gravado)
    @GetMapping("/orcamento")
    public ResponseEntity<OrcamentoResponse> orcar(
            @RequestParam Integer ambienteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaFim) {
        return ResponseEntity.ok(OrcamentoResponse.from(agendamentoService.orcar(ambienteId, horaInicio, horaFim)));
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