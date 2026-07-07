package controle;

import entidades.*;
import excecoes.*;

import java.time.Duration;
import java.util.*;

public class AdministradorSistema {

    private RepositorioAlunos repoAlunos;
    private RepositorioAgendamentos repoAgendamentos;
    private RepositorioAmbientes repoAmbientes;

    public AdministradorSistema() throws FalhaPersistenciaException{
        this.repoAlunos = new RepositorioAlunos();
        this.repoAgendamentos = new RepositorioAgendamentos();
        this.repoAmbientes = new RepositorioAmbientes();
    }
    

    /** ALUNOS */

    /**
     * Cadastra um novo aluno no sistema.
     * verifica se o cpf já existe antes de inserir e lança exceção se duplicado.
     */
    public void cadastrarAluno(Aluno a) throws AlunoJaCadastradoException, FalhaPersistenciaException {
        if(repoAlunos.buscar(a.getCpf()) != null) {
            throw new AlunoJaCadastradoException( "CPF já existe.");
        }
        repoAlunos.inserir(a);
    }

    /**
     * Busca e retorna um aluno pelo cpf.
     * Lança exceção se o cpf não estiver cadastrado.
     */
    public Aluno buscarAluno(String cpf) throws AlunoNaoEncontradoException {
        Aluno a = repoAlunos.buscar(cpf);
        if(a == null) throw new AlunoNaoEncontradoException("Aluno não encontrado.");
        return a;
    }

    /** AMBIENTES */

    /**
     * Busca e retorna um ambiente pelo ID.
     */
    public Ambiente buscarAmbiente(String id) { return repoAmbientes.buscar(id); }

    /**
     * Expõe o repositório de ambientes para que o menuAgendamentos possa consultar ambientes.
     */
    public RepositorioAmbientes getRepositorioAmbiente() { return repoAmbientes; }

    /**
     * Cadastra um novo ambiente no sistema.
     * verifica se o ID já existe antes de inserir e lança exceção se duplicado.
     */
    public void cadastrarAmbiente(Ambiente a) throws AmbienteJaCadastradoException, FalhaPersistenciaException {
        if(repoAmbientes.buscar(a.getId()) != null) {
            throw new AmbienteJaCadastradoException("ID de ambiente já existe.");
        }
        repoAmbientes.inserir(a);
    }

    /**
     * Retorna a lista completa de ambientes cadastrados.
     */
    public List<Ambiente> listarAmbientes() { return repoAmbientes.listarTodos(); }

    /** AGENDAMENTOS */

    /**
     * Tenta registrar uma nova reserva. 
     * Percorre todas as reservas já existentes verificando sobreposição de horário no mesmo espaço e data.
     *  Lança AmbienteIndisponivelException se houver conflito.
     */
    public void realizarAgendamento(Agendamento novo) throws AmbienteIndisponivelException, FalhaPersistenciaException {
        for(Agendamento a : repoAgendamentos.listarTodos()){
            if(a.getAmbiente().getId().equals(novo.getAmbiente().getId()) && a.getDataAgendamento().equals(novo.getDataAgendamento())){
                if(novo.getHoraInicio().isBefore(a.getHoraFim()) && novo.getHoraFim().isAfter(a.getHoraInicio())){
                    throw new AmbienteIndisponivelException("Ambiente já reservado das " + a.getHoraInicio() + " às " + a.getHoraFim());
                }
            }
        }
        novo.recalcularValorTotal();
        repoAgendamentos.inserir(novo);
    }

    /**
     * Cancela o agendamento pelo ID.
     */
    public void cancelarAgendamento(int id) throws AgendamentoNaoEncontradoException, FalhaPersistenciaException { 
        if(repoAgendamentos.buscar(id) == null) throw new AgendamentoNaoEncontradoException("Agendamento não encontrado.");
        repoAgendamentos.remover(id);
    }

    /**
     * Retorna a lista completa de agendamentos.
     */
    public List<Agendamento> listarAgendamentos() { return repoAgendamentos.listarTodos(); }

    /**
     * Delega ao repositório a geração do próximo ID.
     */
    public int gerarIdAgendamento() { return repoAgendamentos.gerarProximoId(); }

    /**
     * Adiciona um serviço adicional a um agendamento existente.
     * Valida se o agendamento existe e se o serviço é válido.
     */
    public void adicionarServicoAoAgendamento(int idAgendamento, ServicoAdicional servico) throws AgendamentoNaoEncontradoException, ServicoInvalidoException, FalhaPersistenciaException {
        Agendamento agendamento = repoAgendamentos.buscar(idAgendamento);
        if(agendamento == null) {
            throw new AgendamentoNaoEncontradoException("Agendamento com ID " + idAgendamento + " não existe.");
        }
        if(servico == null) {
            throw new ServicoInvalidoException("Serviço adicional não pode ser nulo.");
        }
        agendamento.adicionarServico(servico);
        repoAgendamentos.atualizar(agendamento);
    }

    /** RELATÓRIOS */

    /**
     * Relatório 1 - Filtra e retorna todos agendamentos feitos por um determinado aluno.
     */
    public List<Agendamento> relatorioPorAluno(String cpf) {
        List<Agendamento> lista = new ArrayList<>();
        for(Agendamento a : repoAgendamentos.listarTodos()){
            if(a.getAluno().getCpf().equals(cpf)) lista.add(a);
        }
        return lista;
    }

    /**
     * Relatório 2 - Número de agendamentos e horas totais usadas por ambiente.
     */
    public Map<String, Object> relatorioPorAmbiente(){
        Map<String, Object> relatorio = new HashMap<>();
        Map<String, Map<String, Object>> ambientesInfo = new HashMap<>();
        
        for(Agendamento a : repoAgendamentos.listarTodos()){
            String ambienteId = a.getAmbiente().getId();
            ambientesInfo.putIfAbsent(ambienteId, new HashMap<>());
            
            Map<String, Object> info = ambientesInfo.get(ambienteId);
            info.put("ambiente", a.getAmbiente().getNome());
            info.put("quantidade", (int) info.getOrDefault("quantidade", 0) + 1);
            
            long horasAgendamento = Duration.between(a.getHoraInicio(), a.getHoraFim()).toHours();
            long horasAtuais = (long) info.getOrDefault("horas", 0L);
            info.put("horas", horasAtuais + horasAgendamento);
        }
        
        relatorio.put("ambientes", ambientesInfo);
        return relatorio;
    }

    /**
     * Relatório 3 - Faturamento consolidado por dia, ambiente e aluno, com total geral.
     */
    public Map<String, Object> relatorioFaturamento(){
        Map<String, Double> faturamentoPorDia = new LinkedHashMap<>();
        Map<String, Double> faturamentoPorAmbiente = new LinkedHashMap<>();
        Map<String, Double> faturamentoPorAluno = new LinkedHashMap<>();
        double faturamentoTotal = 0.0;

        for(Agendamento a : repoAgendamentos.listarTodos()){
            double valor = a.getValorTotal();
            faturamentoTotal += valor;

            faturamentoPorDia.merge(a.getDataAgendamento().toString(), valor, Double::sum);
            faturamentoPorAmbiente.merge(a.getAmbiente().getNome(), valor, Double::sum);
            faturamentoPorAluno.merge(a.getAluno().getNome(), valor, Double::sum);
        }

        Map<String, Object> relatorio = new LinkedHashMap<>();
        relatorio.put("porDia", faturamentoPorDia);
        relatorio.put("porAmbiente", faturamentoPorAmbiente);
        relatorio.put("porAluno", faturamentoPorAluno);
        relatorio.put("total", faturamentoTotal);
        return relatorio;
    }

    /**
     * Relatório 4 - Quantidade e valor arrecadado por tipo de serviço adicional.
     */
    public Map<String, Object> arrecadamentoPorServico(){
        Map<String, Object> relatorio = new HashMap<>();
        Map<String, Map<String, Object>> servicosInfo = new HashMap<>();
        
        for(Agendamento a : repoAgendamentos.listarTodos()){
            for(ServicoAdicional s : a.getServicosAdicionais()){
                String tipoServico = s.getClass().getSimpleName();
                servicosInfo.putIfAbsent(tipoServico, new HashMap<>());
                
                Map<String, Object> info = servicosInfo.get(tipoServico);
                info.put("quantidade", (int) info.getOrDefault("quantidade", 0) + 1);
                info.put("valorTotal", (double) info.getOrDefault("valorTotal", 0.0) + s.getValorTotal());
            }
        }
        
        relatorio.put("servicos", servicosInfo);
        return relatorio;
    }

    /**
     * Retorna a lista completa de alunos cadastrados.
     */
    public List<Aluno> listarAlunos() { return repoAlunos.listarTodos(); }
}
