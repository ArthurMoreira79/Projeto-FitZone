package com.fitzone.api.controller;

import com.fitzone.api.dto.TipoServicoResponse;
import com.fitzone.domain.model.TipoServico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

// Serviços são sempre adicionados a um agendamento (POST /api/agendamentos/{id}/servicos);
// aqui fica só o catálogo, para o front montar as opções.
@RestController
@RequestMapping("/api/servicos")
public class ServicoController {

    // GET /api/servicos/tipos -> 200 (código, nome, preço e forma de cobrança)
    @GetMapping("/tipos")
    public ResponseEntity<List<TipoServicoResponse>> listarTipos() {
        return ResponseEntity.ok(Arrays.stream(TipoServico.values()).map(TipoServicoResponse::from).toList());
    }
}
