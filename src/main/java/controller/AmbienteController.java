package controller;

import dto.AmbienteRequest;
import dto.AmbienteResponse;
import excecoes.AmbienteJaCadastradoException;
import excecoes.AmbienteNaoEncontradoException;
import excecoes.LimiteAmbienteExcedidoException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import service.AmbienteService;

import java.util.List;

@RestController
@RequestMapping("/api/ambientes")
public class AmbienteController {

    private final AmbienteService ambienteService;

    public AmbienteController(AmbienteService ambienteService) {
        this.ambienteService = ambienteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AmbienteResponse cadastrar(@Valid @RequestBody AmbienteRequest req)
            throws LimiteAmbienteExcedidoException, AmbienteJaCadastradoException {
        return AmbienteResponse.from(ambienteService.cadastrarAmbiente(req.tipo(), req.observacoes()));
    }

    @GetMapping
    public List<AmbienteResponse> listar() {
        return ambienteService.listarAmbientes().stream().map(AmbienteResponse::from).toList();
    }

    @GetMapping("/{id}")
    public AmbienteResponse buscar(@PathVariable String id) throws AmbienteNaoEncontradoException {
        return AmbienteResponse.from(ambienteService.buscarAmbiente(id));
    }
}
