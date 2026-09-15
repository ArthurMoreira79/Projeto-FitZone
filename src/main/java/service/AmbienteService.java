package service;

import entidades.Ambiente;
import entidades.Piscina;
import entidades.SalaCrossfit;
import entidades.SalaMusculacao;
import entidades.SalaYoga;
import entidades.TipoAmbiente;
import excecoes.AmbienteJaCadastradoException;
import excecoes.AmbienteNaoEncontradoException;
import excecoes.LimiteAmbienteExcedidoException;
import org.springframework.stereotype.Service;
import repository.AmbienteRepository;

import java.util.List;

@Service
public class AmbienteService {

    private final AmbienteRepository ambienteRepository;

    public AmbienteService(AmbienteRepository ambienteRepository) {
        this.ambienteRepository = ambienteRepository;
    }

    public Ambiente buscarAmbiente(String id) throws AmbienteNaoEncontradoException {
        return ambienteRepository.findById(id)
                .orElseThrow(() -> new AmbienteNaoEncontradoException("Ambiente não encontrado."));
    }

    public Ambiente cadastrarAmbiente(TipoAmbiente tipo, String observacoes)
            throws LimiteAmbienteExcedidoException, AmbienteJaCadastradoException {
        String maiorId = ambienteRepository.buscarMaiorIdPorTipo(tipo.getClasse());
        int proximoNumero = (maiorId == null) ? tipo.getInicioFaixa() : Integer.parseInt(maiorId) + 1;

        if (proximoNumero > tipo.getFimFaixa()) {
            throw new LimiteAmbienteExcedidoException("Limite de ambientes do tipo " + tipo
                    + " atingido (faixa " + tipo.getInicioFaixa() + "-" + tipo.getFimFaixa() + " esgotada).");
        }

        String id = String.valueOf(proximoNumero);

        Ambiente ambiente = switch (tipo) {
            case MUSCULACAO -> new SalaMusculacao(id, observacoes);
            case YOGA -> new SalaYoga(id, observacoes);
            case CROSSFIT -> new SalaCrossfit(id, observacoes);
            case PISCINA -> new Piscina(id, observacoes);
        };

        // Checagem defensiva: na teoria nunca deveria disparar, já que o ID
        // acima é sempre calculado como "livre", mas é barato garantir.
        if (ambienteRepository.existsById(ambiente.getId())) {
            throw new AmbienteJaCadastradoException("ID de ambiente já existe.");
        }

        return ambienteRepository.save(ambiente);
    }

    public List<Ambiente> listarAmbientes() {
        return ambienteRepository.findAll();
    }
}
