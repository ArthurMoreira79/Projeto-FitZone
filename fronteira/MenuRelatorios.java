package fronteira;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import controle.AdministradorSistema;
import entidades.Agendamento;

public class MenuRelatorios {

    private Scanner leitor;
    private AdministradorSistema admin;

    public MenuRelatorios(AdministradorSistema admin, Scanner leitor) {
        this.admin = admin;
        this.leitor = leitor;
    }

    //Exibe o menu de relatórios em loop, com as 4 opções, até o usuário voltar.
    public void exibir() {
        int opcao = -1;
        while (opcao != 0) {
            try {
                System.out.println("\n===== MENU DE RELATÓRIOS =====");
                System.out.println("1. Agendamentos por Aluno");
                System.out.println("2. Utilização de Ambientes");
                System.out.println("3. Faturamento Total");
                System.out.println("4. Serviços Adicionais");
                System.out.println("0. Voltar");
                System.out.print("Escolha: ");

                opcao = Integer.parseInt(leitor.nextLine());

                switch (opcao) {
                    case 1 -> relatorioPorAluno();
                    case 2 -> relatorioPorAmbiente();
                    case 3 -> relatorioFaturamento();
                    case 4 -> arrecadamentoPorServico();
                    case 0 -> {} //volta ao menu principal
                    default -> System.out.println("Opção inválida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Erro: Entrada numérica inválida.");
            } catch (Exception e) {
                System.out.println("Erro inesperado: " + e.getMessage());
            }
        }
    }

    private void relatorioPorAluno() {
        System.out.println("\n--- RELATÓRIO: AGENDAMENTOS POR ALUNO ---");
        System.out.print("CPF do Aluno: ");
        String cpf = leitor.nextLine();

        List<Agendamento> agendamentos = admin.relatorioPorAluno(cpf);

        if (agendamentos.isEmpty()) {
            System.out.println("Nenhum agendamento encontrado para este aluno.");
        } else {
            System.out.println("Agendamentos encontrados: " + agendamentos.size());
            System.out.println("-------------------------------");
            agendamentos.forEach(a -> {
                System.out.println(a);
                System.out.println("-------------------------------");
            });
        }
    }

    @SuppressWarnings("unchecked")
    private void relatorioPorAmbiente() {
        System.out.println("\n--- RELATÓRIO: UTILIZAÇÃO DE AMBIENTES ---");
        Map<String, Object> relatorio = admin.relatorioPorAmbiente();
        Map<String, Object> ambientes = (Map<String, Object>) relatorio.get("ambientes");

        if (ambientes == null || ambientes.isEmpty()) {
            System.out.println("Nenhum ambiente com agendamentos cadastrados.");
        } else {
            ambientes.forEach((id, info) -> {
                Map<String, Object> dados = (Map<String, Object>) info;
                String nomeAmbiente = (String) dados.get("ambiente");
                int quantidade = (int) dados.get("quantidade");
                long horas = (long) dados.get("horas");
                System.out.println(nomeAmbiente + " | Agendamentos: " + quantidade + " | Horas: " + horas);
            });
        }
    }

    @SuppressWarnings("unchecked")
    private void relatorioFaturamento() {
        System.out.println("\n--- RELATÓRIO: FATURAMENTO ---");
        Map<String, Object> relatorio = admin.relatorioFaturamento();

        System.out.println("\n--- FATURAMENTO POR DIA ---");
        Map<String, Double> porDia = (Map<String, Double>) relatorio.get("porDia");
        porDia.forEach((data, valor) -> System.out.println(data + " -> R$ " + String.format("%.2f", valor)));

        System.out.println("\n--- FATURAMENTO POR AMBIENTE ---");
        Map<String, Double> porAmbiente = (Map<String, Double>) relatorio.get("porAmbiente");
        porAmbiente.forEach((ambiente, valor) -> System.out.println(ambiente + " -> R$ " + String.format("%.2f", valor)));

        System.out.println("\n--- FATURAMENTO POR ALUNO ---");
        Map<String, Double> porAluno = (Map<String, Double>) relatorio.get("porAluno");
        porAluno.forEach((aluno, valor) -> System.out.println(aluno + " -> R$ " + String.format("%.2f", valor)));

        System.out.println("\n--- FATURAMENTO TOTAL ---");
        double total = (double) relatorio.get("total");
        System.out.printf("R$ %.2f%n", total);
    }

    @SuppressWarnings("unchecked")
    private void arrecadamentoPorServico() {
        System.out.println("\n--- RELATÓRIO: SERVIÇOS ADICIONAIS ---");
        Map<String, Object> relatorio = admin.arrecadamentoPorServico();
        Map<String, Object> servicos = (Map<String, Object>) relatorio.get("servicos");

        if (servicos == null || servicos.isEmpty()) {
            System.out.println("Nenhum serviço adicional registrado.");
        } else {
            servicos.forEach((tipo, info) -> {
                Map<String, Object> dados = (Map<String, Object>) info;
                int quantidade = (int) dados.get("quantidade");
                double valorTotal = (double) dados.get("valorTotal");
                System.out.println(tipo + " | Quantidade: " + quantidade + " | Valor arrecadado: R$ " + String.format("%.2f", valorTotal));
            });
        }
    }
}