package fronteira;

import java.util.List;
import java.time.*;

import controle.AdministradorSistema;
import entidades.*;
import excecoes.*;

public class MenuAgendamentos {

    private LeitorEntrada leitor;
    private AdministradorSistema admin;

    public MenuAgendamentos(AdministradorSistema admin, LeitorEntrada leitor) {
        this.admin = admin;
        this.leitor = leitor;
    }

    /**
     * Exibe o submenu de agendamentos em loop: criar, cancelar, adicionar serviço a uma reserva já existente e listar.
     * Captura entradas inválidas sem encerrar o sistema.
     */
    public void exibir() {
        int opcao = -1;
        while (opcao != 0) {
            try {
                ConsoleUtil.titulo("MENU DE AGENDAMENTOS");
                System.out.println("1. Nova reserva");
                System.out.println("2. Cancelar reserva");
                System.out.println("3. Adicionar serviço a uma reserva existente");
                System.out.println("4. Listar todos os agendamentos");
                System.out.println("0. Voltar");
                opcao = leitor.lerInteiro("Escolha: ");

                switch (opcao) {
                    case 1 -> novaReserva();
                    case 2 -> cancelar();
                    case 3 -> adicionarServico();
                    case 4 -> listar();
                    case 0 -> {} /* volta ao menu principal */
                    default -> System.out.println("Opção inválida.");
                }
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }
    }

    /**
     * Conduz o fluxo completo de criação de um novo agendamento:
     * 1. Busca o aluno pelo cpf - lança AlunoNaoEncontradoException se não existir.
     * 2. Busca o ambiente pelo ID via AdministradorSistema (nunca acessa o repositório direto).
     * 3. Lê data, hora de início e fim.
     * 4. Abre um submenu de serviços adicionais em loop até o usuário digitar 0.
     * 5. Chama admin.realizarAgendamento() - lança AmbienteIndisponivelException se houver conflito.
     * 6. Exibe o resumo do agendamento criado.
     */
    private void novaReserva() throws Exception {
        ConsoleUtil.subtitulo("NOVO AGENDAMENTO");

        String cpf = leitor.lerCpf("CPF do Aluno (11 dígitos): ");
        Aluno aluno = admin.buscarAluno(cpf);

        String idAmbiente = leitor.lerTextoObrigatorio("ID do Ambiente: ");
        Ambiente ambiente = admin.buscarAmbiente(idAmbiente);
        if (ambiente == null) {
            throw new IllegalArgumentException("Ambiente com ID '" + idAmbiente + "' não encontrado.");
        }

        LocalDate data = leitor.lerDataNaoAnterior("Data (AAAA-MM-DD): ");

        LocalTime inicio = leitor.lerHora("Início (HH:MM): ");

        LocalTime fim = leitor.lerHoraApos("Fim (HH:MM): ", inicio);

        int idAgendamento = admin.gerarIdAgendamento();
        Agendamento agendamento = new Agendamento(idAgendamento, aluno, ambiente, data, inicio, fim);

        selecionarServicos(agendamento);

        admin.realizarAgendamento(agendamento);

        System.out.println("\n✔ Agendamento realizado com sucesso!");
        ConsoleUtil.linha();
        System.out.println(agendamento);
        ConsoleUtil.respiro();
    }

    /**
     * Submenu de seleção de serviços adicionais usado na criação do agendamento.
     */
    private void selecionarServicos(Agendamento agendamento) throws ServicoInvalidoException {
        int opcaoServico = -1;
        while (opcaoServico != 0) {
            ConsoleUtil.subtitulo("SERVIÇOS ADICIONAIS");
            System.out.println("1. Avaliação Física         (R$70,00)");
            System.out.println("2. Nutricionista            (R$80,00)");
            System.out.println("3. Personal Trainer         (R$50,00)");
            System.out.println("4. Locker da Academia       (R$5,00/unidade)");
            System.out.println("0. Finalizar e confirmar agendamento");
            opcaoServico = leitor.lerInteiro("Escolha: ");

            switch (opcaoServico) {
                case 1 -> {
                    agendamento.adicionarServico(new AvaliacaoFisica());
                    System.out.println("Avaliação Física adicionada. (+R$70,00)");
                }
                case 2 -> {
                    agendamento.adicionarServico(new Nutricionista());
                    System.out.println("Nutricionista adicionado. (+R$80,00)");
                }
                case 3 -> {
                    agendamento.adicionarServico(new PersonalTrainer());
                    System.out.println("Personal Trainer adicionado. (+R$50,00)");
                }
                case 4 -> {
                    int qtd = leitor.lerInteiroPositivo("Quantidade de lockers: ");
                    agendamento.adicionarServico(new LockerAcademia(qtd));
                    System.out.println("Locker adicionado. (+R$" + (qtd * 5.0) + ")");
                }
                case 0 -> System.out.println("Finalizando escolha de serviços...");
                default -> throw new ServicoInvalidoException("Opção de serviço inexistente.");
            }
        }
    }

    /**
     * Solicita o ID de uma reserva já existente e adiciona um novo serviço a ela, delegando ao AdministradorSistema.adicionarServicoAoAgendamento() (que também persiste a alteração).
     */
    private void adicionarServico() throws AgendamentoNaoEncontradoException, ServicoInvalidoException, FalhaPersistenciaException {
        ConsoleUtil.subtitulo("ADICIONAR SERVIÇO A RESERVA EXISTENTE");
        int id = leitor.lerInteiro("ID da reserva: ");

        System.out.println("1. Avaliação Física         (R$70,00)");
        System.out.println("2. Nutricionista            (R$80,00)");
        System.out.println("3. Personal Trainer         (R$50,00)");
        System.out.println("4. Locker da Academia       (R$5,00/unidade)");
        int opcao = leitor.lerInteiro("Escolha: ");

        ServicoAdicional servico = switch (opcao) {
            case 1 -> new AvaliacaoFisica();
            case 2 -> new Nutricionista();
            case 3 -> new PersonalTrainer();
            case 4 -> {
                int qtd = leitor.lerInteiroPositivo("Quantidade de lockers: ");
                yield new LockerAcademia(qtd);
            }
            default -> throw new ServicoInvalidoException("Opção de serviço inexistente.");
        };

        admin.adicionarServicoAoAgendamento(id, servico);
        System.out.println("\nServiço adicionado com sucesso à reserva #" + id + ".");
        ConsoleUtil.respiro();
    }

    /**
     * Solicita o ID de uma reserva e a cancela chamando admin.cancelarAgendamento().
     */
    private void cancelar() throws AgendamentoNaoEncontradoException, FalhaPersistenciaException {
        ConsoleUtil.subtitulo("CANCELAR AGENDAMENTO");
        int id = leitor.lerInteiro("ID da reserva: ");
        admin.cancelarAgendamento(id);
        System.out.println("\nAgendamento #" + id + " cancelado com sucesso.");
        ConsoleUtil.respiro();
    }

    /**
     * Lista todos os agendamentos registrados no sistema em formato de tabela.
     */
    private void listar() {
        ConsoleUtil.subtitulo("AGENDAMENTOS CADASTRADOS");
        List<Agendamento> lista = admin.listarAgendamentos();

        if (lista.isEmpty()) {
            System.out.println("Nenhum agendamento cadastrado.");
        } else {
            System.out.println();
            System.out.printf("%-4s %-18s %-18s %-12s %-13s %-10s%n",
                    "ID", "ALUNO", "AMBIENTE", "DATA", "HORÁRIO", "TOTAL");
            ConsoleUtil.linha();
            lista.forEach(a -> System.out.printf("%-4d %-18s %-18s %-12s %-13s %-10s%n",
                    a.getId(), a.getAluno().getNome(), a.getAmbiente().getNome(),
                    a.getDataAgendamento(), a.getHoraInicio() + "-" + a.getHoraFim(),
                    String.format("R$ %.2f", a.getValorTotal())));
        }
        ConsoleUtil.respiro();
    }
}