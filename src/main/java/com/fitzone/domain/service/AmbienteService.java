package com.fitzone.domain.service;

import com.fitzone.domain.exception.AmbienteJaCadastradoException;
import com.fitzone.domain.exception.AmbienteNaoEncontradoException;
import com.fitzone.domain.exception.LimiteAmbienteExcedidoException;
import com.fitzone.domain.exception.RegistroEmUsoException;
import com.fitzone.domain.model.*;
import com.fitzone.domain.repository.AgendamentoRepository;
import com.fitzone.domain.repository.AmbienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

@Service
public class AmbienteService {
    
    private final AmbienteRepository ambienteRepository;
    // o repositório, e não o AgendamentoService: o AgendamentoService já depende deste service
    private final AgendamentoRepository agendamentoRepository;

    public AmbienteService(AmbienteRepository ambienteRepository, AgendamentoRepository agendamentoRepository) {
        this.ambienteRepository = ambienteRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    /**
     * Cadastra um ambiente do tipo informado, com o primeiro id livre da faixa
     * do TipoAmbiente e nome padronizado.
     * Ids de ambientes excluídos voltam a ser usados: com 10 ids por tipo, "MAX + 1"
     * esgotaria a faixa mesmo com vagas no meio. Reaproveitar é seguro porque só é
     * possível excluir ambiente sem agendamentos (não há histórico ligado ao id).
     */
    @Transactional
    public Ambiente cadastrar(TipoAmbiente tipo, String observacoes) {
        Set<Integer> usados = ambienteRepository.buscarIdsPorTipo(tipo.getClasse());
        int proximoId = IntStream.rangeClosed(tipo.getInicioFaixa(), tipo.getFimFaixa())
                .filter(id -> !usados.contains(id))
                .findFirst()
                .orElseThrow(() -> new LimiteAmbienteExcedidoException("Limite de ambientes do tipo " + tipo.getNome()
                        + " atingido (faixa " + tipo.getInicioFaixa() + "-" + tipo.getFimFaixa() + " esgotada)."));

        //Como o id NÃO é gerado pelo banco, o save() com id preenchido faz um merge:
        //se esse id já existe, ele SOBRESCREVERIA o ambiente em vez de dar erro. 
        //Esta checagem impede isso. 
        if(ambienteRepository.existsById(proximoId)) {
            throw new AmbienteJaCadastradoException("ID " + proximoId + " já está em uso.");
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

        // mensagem clara (409); o ON DELETE RESTRICT do banco segue como última linha de defesa
        long agendamentos = agendamentoRepository.countByAmbiente_Id(id);
        if (agendamentos > 0) {
            throw new RegistroEmUsoException("O ambiente " + ambiente.getNome() + " tem " + agendamentos
                    + (agendamentos == 1 ? " agendamento" : " agendamentos")
                    + " e não pode ser excluído. Cancele os agendamentos antes.");
        }

        ambienteRepository.delete(ambiente);
        ambienteRepository.flush();
    }
}
