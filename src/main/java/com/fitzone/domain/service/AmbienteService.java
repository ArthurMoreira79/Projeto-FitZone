package com.fitzone.domain.service;

import com.fitzone.domain.exception.AmbienteJaCadastradoException;
import com.fitzone.domain.exception.AmbienteNaoEncontradoException;
import com.fitzone.domain.exception.LimiteAmbienteExcedidoException;
import com.fitzone.domain.model.*;
import com.fitzone.domain.repository.AmbienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AmbienteService {
    
    private final AmbienteRepository ambienteRepository;

    public AmbienteService(AmbienteRepository ambienteRepository) {
        this.ambienteRepository = ambienteRepository;
    }

    /**
     * Cadastra um ambiente do tipo informado, gerando o próximo id livre da faixa
     * do TipoAmbiente e nome padronizado
     */
    @Transactional
    public Ambiente cadastrar(TipoAmbiente tipo, String observacoes) {
        Integer maiorId = ambienteRepository.buscarMaiorIdPorTipo(tipo.getClasse());
        int proximoId = (maiorId == null) ? tipo.getInicioFaixa() : maiorId + 1;

        if (proximoId > tipo.getFimFaixa()) {
            throw new LimiteAmbienteExcedidoException("Limite de ambientes do tipo " + tipo + "atingido (faixa " + tipo.getInicioFaixa() + "-" + tipo.getFimFaixa() + " esgotada).");
        }

        //Como o id NÃO é gerado pelo banco, o save() com id preenchido faz um merge:
        //se esse id já existe, ele SOBRESCREVERIA o ambiente em vez de dar erro. 
        //Esta checagem impede isso. 
        if(ambienteRepository.existsById(proximoId)) {
            throw new AmbienteJaCadastradoException("ID " + proximoId + "já está em uso.");
        }

        Ambiente ambiente = switch(tipo) {
            case MUSCULACAO -> new SalaMusculacao(proximoId, observacoes);
            case YOGA       -> new SalaYoga(proximoId, observacoes);
            case CROSSFIT   -> new SalaCrossfit(proximoId, observacoes);
            case PISCINA    -> new Piscina(proximoId, observacoes);
        };

        return ambienteRepository.save(ambiente);
    }

    @Transactional(readOnly = true)
    public List<Ambiente> listarTodos() {
        return ambienteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Ambiente buscarPorId(Integer id) {
        return ambienteRepository.findById(id)
                .orElseThrow(() -> new AmbienteNaoEncontradoException("Ambiente de id " + id + " não encontrado."));
    }

    /**
     * Só as observações são editáveis: id, nome e valor/hora são definidos pelo tipo.
     */
    @Transactional
    public Ambiente atualizarObservacoes(Integer id, String observacoes) {
        Ambiente ambiente = buscarPorId(id);
        ambiente.setObservacoes(observacoes);
        return ambiente;
    }

    @Transactional
    public void excluir(Integer id) {
        Ambiente ambiente = buscarPorId(id);
        ambienteRepository.delete(ambiente);
        // ambiente com agendamentos: ON DELETE RESTRICT falha aqui (409 no handler)
        ambienteRepository.flush();
    }
}
