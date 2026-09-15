package controller;

import dto.AgendamentoRequest;
import dto.AgendamentoResponse;
import dto.ServicoRequest;
import excecoes.AgendamentoNaoEncontradoException;
import excecoes.AlunoNaoEncontradoException;
import excecoes.AmbienteIndisponivelException;
import excecoes.AmbienteNaoEncontradoException;
import excecoes.ServicoInvalidoException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import service.AgendamentoService;

import java.util.List;

@RestController
@RequestMapping("/api/agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgendamentoResponse realizar(@Valid @RequestBody AgendamentoRequest req)
            throws AlunoNaoEncontradoException, AmbienteNaoEncontradoException, AmbienteIndisponivelException {
        return AgendamentoResponse.from(agendamentoService.realizarAgendamento(req));
    }

    @GetMapping
    public List<AgendamentoResponse> listar() {
        return agendamentoService.listarAgendamentos().stream().map(AgendamentoResponse::from).toList();
    }

    @GetMapping("/{id}")
    public AgendamentoResponse buscar(@PathVariable int id) throws AgendamentoNaoEncontradoException {
        return AgendamentoResponse.from(agendamentoService.buscarAgendamento(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(@PathVariable int id) throws AgendamentoNaoEncontradoException {
        agendamentoService.cancelarAgendamento(id);
    }

    @PostMapping("/{id}/servicos")
    public AgendamentoResponse adicionarServico(@PathVariable int id, @Valid @RequestBody ServicoRequest req)
            throws AgendamentoNaoEncontradoException, ServicoInvalidoException {
        return AgendamentoResponse.from(agendamentoService.adicionarServicoAoAgendamento(id, req));
    }
}
