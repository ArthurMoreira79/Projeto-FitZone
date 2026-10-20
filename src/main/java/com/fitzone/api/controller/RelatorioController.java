package com.fitzone.api.controller;

import com.fitzone.api.dto.RelatorioAlunoDTO;
import com.fitzone.api.dto.RelatorioAmbienteDTO;
import com.fitzone.api.dto.RelatorioFaturamentoDTO;
import com.fitzone.api.dto.RelatorioServicoDTO;
import com.fitzone.domain.service.RelatorioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/alunos/{id}")
    public ResponseEntity<RelatorioAlunoDTO> porAluno(@PathVariable Integer id) {
        return ResponseEntity.ok(relatorioService.porAluno(id));
    }

    @GetMapping("/ambientes")
    public ResponseEntity<List<RelatorioAmbienteDTO>> porAmbiente() {
        return ResponseEntity.ok(relatorioService.porAmbiente());
    }

    @GetMapping("/faturamento")
    public ResponseEntity<RelatorioFaturamentoDTO> faturamento() {
        return ResponseEntity.ok(relatorioService.faturamento());
    }

    @GetMapping("/servicos")
    public ResponseEntity<List<RelatorioServicoDTO>> porServico() {
        return ResponseEntity.ok(relatorioService.porServico());
    }
}