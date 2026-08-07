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
        // Fase 2: força a inicialização do EntityManagerFactory e valida que o
        // banco MySQL está acessível já na subida do sistema, preservando o
        // comportamento da Fase 1 (falhar cedo, com uma mensagem clara, em vez
        // de só descobrir o problema no primeiro cadastro).
        try {
            JPAUtil.getEntityManager().close();
        } catch (Exception e) {
            throw new FalhaPersistenciaException("Não foi possível conectar ao banco de dados MySQL. Verifique se ele está no ar e se as credenciais em persistence.xml estão corretas. Detalhe: " + e.getMessage());
        }

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
    /**
     * Cadastra um novo ambiente do tipo informado, gerando automaticamente
     * o próximo ID disponível dentro da faixa reservada a esse tipo (ver
     * {@link TipoAmbiente}) e um nome padronizado ("Sala de Musculação 101").
     *
     * @param tipo tipo do ambiente a criar
     * @param observacoes texto livre opcional (pode ser null/vazio)
     * @return o ambiente já criado e persistido, para a camada de fronteira exibir
     * @throws LimiteAmbientesExcedidoException se a faixa de IDs do tipo já estiver cheia
     */
    public Ambiente cadastrarAmbiente(TipoAmbiente tipo, String observacoes) throws LimiteAmbienteExcedidoException, AmbienteJaCadastradoException, FalhaPersistenciaException {
        String maiorId = repoAmbientes.buscarMaiorIdPorTipo(tipo.getClasse());
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
        if (repoAmbientes.buscar(ambiente.getId()) != null) {
            throw new AmbienteJaCadastradoException("ID de ambiente já existe.");
        }

        repoAmbientes.inserir(ambiente);
        return ambiente;
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
        List<Agendamento> conflitos = repoAgendamentos.buscarConflitos(
                novo.getAmbiente().getId(), novo.getDataAgendamento(), novo.getHoraInicio(), novo.getHoraFim());
        if (!conflitos.isEmpty()) {
            Agendamento a = conflitos.get(0);
            throw new AmbienteIndisponivelException("Ambiente já reservado das " + a.getHoraInicio() + " às " + a.getHoraFim());
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
            if (horasAgendamento <= 0) horasAgendamento = 1; // mesma regra de Agendamento.calculaValorTotal()
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