package fronteira;

import java.util.List;
import java.util.Map;
import controle.AdministradorSistema;
import entidades.Agendamento;

public class MenuRelatorios {

    private LeitorEntrada leitor;
    private AdministradorSistema admin;

    public MenuRelatorios(AdministradorSistema admin, LeitorEntrada leitor) {
        this.admin = admin;
        this.leitor = leitor;
    }

    /**
     * Exibe o menu de relatórios em loop, com as 4 opções, até o usuário voltar.
     */
    public void exibir() {
        int opcao = -1;
        while (opcao != 0) {
            try {
                ConsoleUtil.titulo("MENU DE RELATÓRIOS");
                System.out.println("1. Agendamentos por Aluno");
                System.out.println("2. Utilização de Ambientes");
                System.out.println("3. Faturamento Total");
                System.out.println("4. Serviços Adicionais");
                System.out.println("0. Voltar");
                opcao = leitor.lerInteiro("Escolha: ");

                switch (opcao) {
                    case 1 -> relatorioPorAluno();
                    case 2 -> relatorioPorAmbiente();
                    case 3 -> relatorioFaturamento();
                    case 4 -> arrecadamentoPorServico();
                    case 0 -> {} /* volta ao menu principal */
                    default -> System.out.println("Opção inválida.");
                }
            } catch (Exception e) {
                System.out.println("Erro inesperado: " + e.getMessage());
            }
        }
    }

    private void relatorioPorAluno() {
        ConsoleUtil.subtitulo("RELATÓRIO: AGENDAMENTOS POR ALUNO");
        String cpf = leitor.lerCpf("CPF do Aluno (11 dígitos): ");

        List<Agendamento> agendamentos = admin.relatorioPorAluno(cpf);

        if (agendamentos.isEmpty()) {
            System.out.println("\nNenhum agendamento encontrado para este aluno.");
        } else {
            System.out.println("\nAgendamentos encontrados: " + agendamentos.size());
            System.out.printf("%-4s %-18s %-12s %-13s %-10s%n", "ID", "AMBIENTE", "DATA", "HORÁRIO", "TOTAL");
            ConsoleUtil.linha();
            agendamentos.forEach(a -> System.out.printf("%-4d %-18s %-12s %-13s %-10s%n",
                    a.getId(), a.getAmbiente().getNome(), a.getDataAgendamento(),
                    a.getHoraInicio() + "-" + a.getHoraFim(), String.format("R$ %.2f", a.getValorTotal())));
        }
        ConsoleUtil.respiro();
    }

    @SuppressWarnings("unchecked")
    private void relatorioPorAmbiente() {
        ConsoleUtil.subtitulo("RELATÓRIO: UTILIZAÇÃO DE AMBIENTES");
        Map<String, Object> relatorio = admin.relatorioPorAmbiente();
        Map<String, Object> ambientes = (Map<String, Object>) relatorio.get("ambientes");

        if (ambientes == null || ambientes.isEmpty()) {
            System.out.println("\nNenhum ambiente com agendamentos cadastrados.");
        } else {
            System.out.println();
            System.out.printf("%-22s %-14s %-10s%n", "AMBIENTE", "AGENDAMENTOS", "HORAS");
            ConsoleUtil.linha();
            ambientes.forEach((id, info) -> {
                Map<String, Object> dados = (Map<String, Object>) info;
                String nomeAmbiente = (String) dados.get("ambiente");
                int quantidade = (int) dados.get("quantidade");
                long horas = (long) dados.get("horas");
                System.out.printf("%-22s %-14d %-10d%n", nomeAmbiente, quantidade, horas);
            });
        }
        ConsoleUtil.respiro();
    }

    @SuppressWarnings("unchecked")
    private void relatorioFaturamento() {
        ConsoleUtil.subtitulo("RELATÓRIO: FATURAMENTO");
        Map<String, Object> relatorio = admin.relatorioFaturamento();

        System.out.println("\nPor dia:");
        System.out.printf("%-15s %-10s%n", "DATA", "VALOR");
        ConsoleUtil.linha();
        Map<String, Double> porDia = (Map<String, Double>) relatorio.get("porDia");
        porDia.forEach((data, valor) -> System.out.printf("%-15s %-10s%n", data, String.format("R$ %.2f", valor)));

        System.out.println("\nPor ambiente:");
        System.out.printf("%-22s %-10s%n", "AMBIENTE", "VALOR");
        ConsoleUtil.linha();
        Map<String, Double> porAmbiente = (Map<String, Double>) relatorio.get("porAmbiente");
        porAmbiente.forEach((ambiente, valor) -> System.out.printf("%-22s %-10s%n", ambiente, String.format("R$ %.2f", valor)));

        System.out.println("\nPor aluno:");
        System.out.printf("%-22s %-10s%n", "ALUNO", "VALOR");
        ConsoleUtil.linha();
        Map<String, Double> porAluno = (Map<String, Double>) relatorio.get("porAluno");
        porAluno.forEach((aluno, valor) -> System.out.printf("%-22s %-10s%n", aluno, String.format("R$ %.2f", valor)));

        double total = (double) relatorio.get("total");
        System.out.println();
        ConsoleUtil.linha();
        System.out.printf("TOTAL GERAL: R$ %.2f%n", total);
        ConsoleUtil.respiro();
    }

    @SuppressWarnings("unchecked")
    private void arrecadamentoPorServico() {
        ConsoleUtil.subtitulo("RELATÓRIO: SERVIÇOS ADICIONAIS");
        Map<String, Object> relatorio = admin.arrecadamentoPorServico();
        Map<String, Object> servicos = (Map<String, Object>) relatorio.get("servicos");

        if (servicos == null || servicos.isEmpty()) {
            System.out.println("\nNenhum serviço adicional registrado.");
        } else {
            System.out.println();
            System.out.printf("%-25s %-12s %-10s%n", "SERVIÇO", "QUANTIDADE", "VALOR");
            ConsoleUtil.linha();
            servicos.forEach((tipo, info) -> {
                Map<String, Object> dados = (Map<String, Object>) info;
                int quantidade = (int) dados.get("quantidade");
                double valorTotal = (double) dados.get("valorTotal");
                System.out.printf("%-25s %-12d %-10s%n", tipo, quantidade, String.format("R$ %.2f", valorTotal));
            });
        }
        ConsoleUtil.respiro();
    }
}