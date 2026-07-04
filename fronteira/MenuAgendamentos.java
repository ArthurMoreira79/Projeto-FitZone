package fronteira;

import java.util.List;
import java.util.Scanner;
import java.time.*;
import controle.AdministradorSistema;
import entidades.*;
import excecoes.*;

public class MenuAgendamentos {

    private Scanner leitor;
    private AdministradorSistema admin;

    public MenuAgendamentos(AdministradorSistema admin, Scanner leitor) {
        this.admin = admin;
        this.leitor = leitor;
    }

    //Exibe o submenu de agendamentos em loop: criar, cancelar, adicionar serviço a uma
    //reserva já existente e listar. Captura entradas inválidas sem encerrar o sistema.
    public void exibir() {
        int opcao = -1;
        while (opcao != 0) {
            try {
                System.out.println("\n===== MENU DE AGENDAMENTOS =====");
                System.out.println("1. Nova reserva");
                System.out.println("2. Cancelar reserva");
                System.out.println("3. Adicionar serviço a uma reserva existente");
                System.out.println("4. Listar todos os agendamentos");
                System.out.println("0. Voltar");
                System.out.print("Escolha: ");

                opcao = Integer.parseInt(leitor.nextLine());

                switch (opcao) {
                    case 1 -> novaReserva();
                    case 2 -> cancelar();
                    case 3 -> adicionarServico();
                    case 4 -> listar();
                    case 0 -> {} //volta ao menu principal
                    default -> System.out.println("Opção inválida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Erro: Entrada numérica inválida.");
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }
    }

    //Conduz o fluxo completo de criação de um novo agendamento:
    //1. Busca o aluno pelo cpf - lança AlunoNaoEncontradoException se não existir.
    //2. Busca o ambiente pelo ID via AdministradorSistema (nunca acessa o repositório direto).
    //3. Lê data, hora de início e fim.
    //4. Abre um submenu de serviços adicionais em loop até o usuário digitar 0.
    //5. Chama admin.realizarAgendamento() - lança AmbienteIndisponivelException se houver conflito.
    //6. Exibe o resumo do agendamento criado.
    private void novaReserva() throws Exception {
        System.out.println("\n--- NOVO AGENDAMENTO ---");

        System.out.print("CPF do Aluno: ");
        String cpf = leitor.nextLine();
        Aluno aluno = admin.buscarAluno(cpf);

        System.out.print("ID do Ambiente: ");
        String idAmbiente = leitor.nextLine();
        Ambiente ambiente = admin.buscarAmbiente(idAmbiente);
        if (ambiente == null) {
            throw new IllegalArgumentException("Ambiente com ID '" + idAmbiente + "' não encontrado.");
        }

        System.out.print("Data (AAAA-MM-DD): ");
        LocalDate data = LocalDate.parse(leitor.nextLine());

        System.out.print("Início (HH:MM): ");
        LocalTime inicio = LocalTime.parse(leitor.nextLine());

        System.out.print("Fim (HH:MM): ");
        LocalTime fim = LocalTime.parse(leitor.nextLine());

        int idAgendamento = admin.gerarIdAgendamento();
        Agendamento agendamento = new Agendamento(idAgendamento, aluno, ambiente, data, inicio, fim);

        selecionarServicos(agendamento);

        admin.realizarAgendamento(agendamento);

        System.out.println("\n✔ Agendamento realizado com sucesso!");
        System.out.println(agendamento);
    }

    //Submenu de seleção de serviços adicionais usado na criação do agendamento.
    private void selecionarServicos(Agendamento agendamento) throws ServicoInvalidoException {
        int opcaoServico = -1;
        while (opcaoServico != 0) {
            System.out.println("\n--- SERVIÇOS ADICIONAIS ---");
            System.out.println("1. Avaliação Física         (R$70,00)");
            System.out.println("2. Nutricionista            (R$80,00)");
            System.out.println("3. Personal Trainer         (R$50,00)");
            System.out.println("4. Locker da Academia       (R$5,00/unidade)");
            System.out.println("0. Finalizar e confirmar agendamento");
            System.out.print("Escolha: ");

            opcaoServico = Integer.parseInt(leitor.nextLine());

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
                    System.out.print("Quantidade de lockers: ");
                    int qtd = Integer.parseInt(leitor.nextLine());
                    agendamento.adicionarServico(new LockerAcademia(qtd));
                    System.out.println("Locker adicionado. (+R$" + (qtd * 5.0) + ")");
                }
                case 0 -> System.out.println("Finalizando escolha de serviços...");
                default -> throw new ServicoInvalidoException("Opção de serviço inexistente.");
            }
        }
    }

    //Solicita o ID de uma reserva já existente e adiciona um novo serviço a ela,
    //delegando ao AdministradorSistema.adicionarServicoAoAgendamento() (que também persiste a alteração).
    private void adicionarServico() throws AgendamentoNaoEncontradoException, ServicoInvalidoException, FalhaPersistenciaException {
        System.out.println("\n--- ADICIONAR SERVIÇO A RESERVA EXISTENTE ---");
        System.out.print("ID da reserva: ");
        int id = Integer.parseInt(leitor.nextLine());

        System.out.println("1. Avaliação Física         (R$70,00)");
        System.out.println("2. Nutricionista            (R$80,00)");
        System.out.println("3. Personal Trainer         (R$50,00)");
        System.out.println("4. Locker da Academia       (R$5,00/unidade)");
        System.out.print("Escolha: ");
        int opcao = Integer.parseInt(leitor.nextLine());

        ServicoAdicional servico = switch (opcao) {
            case 1 -> new AvaliacaoFisica();
            case 2 -> new Nutricionista();
            case 3 -> new PersonalTrainer();
            case 4 -> {
                System.out.print("Quantidade de lockers: ");
                int qtd = Integer.parseInt(leitor.nextLine());
                yield new LockerAcademia(qtd);
            }
            default -> throw new ServicoInvalidoException("Opção de serviço inexistente.");
        };

        admin.adicionarServicoAoAgendamento(id, servico);
        System.out.println("Serviço adicionado com sucesso à reserva #" + id + ".");
    }

    //Solicita o ID de uma reserva e a cancela chamando admin.cancelarAgendamento().
    private void cancelar() throws AgendamentoNaoEncontradoException, FalhaPersistenciaException {
        System.out.println("\n--- CANCELAR AGENDAMENTO ---");
        System.out.print("ID da reserva: ");
        int id = Integer.parseInt(leitor.nextLine());
        admin.cancelarAgendamento(id);
        System.out.println("Agendamento #" + id + " cancelado com sucesso.");
    }

    //Lista todos os agendamentos registrados no sistema.
    private void listar() {
        List<Agendamento> lista = admin.listarAgendamentos();

        if (lista.isEmpty()) {
            System.out.println("Nenhum agendamento cadastrado.");
            return;
        }

        System.out.println("\n--- AGENDAMENTOS CADASTRADOS ---");
        lista.forEach(a -> {
            System.out.println(a);
            System.out.println("-----------------------------");
        });
    }
}