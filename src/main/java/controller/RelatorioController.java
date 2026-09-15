package controller;

import dto.AgendamentoResponse;
import dto.RelatorioAmbienteDTO;
import dto.RelatorioFaturamentoDTO;
import dto.RelatorioServicoDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.RelatorioService;

import java.util.List;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/alunos/{cpf}")
    public List<AgendamentoResponse> porAluno(@PathVariable String cpf) {
        return relatorioService.relatorioPorAluno(cpf).stream().map(AgendamentoResponse::from).toList();
    }

    @GetMapping("/ambientes")
    public List<RelatorioAmbienteDTO> porAmbiente() {
        return relatorioService.relatorioPorAmbiente();
    }

    @GetMapping("/faturamento")
    public RelatorioFaturamentoDTO faturamento() {
        return relatorioService.relatorioFaturamento();
    }

    @GetMapping("/servicos")
    public List<RelatorioServicoDTO> porServico() {
        return relatorioService.arrecadamentoPorServico();
    }
}
