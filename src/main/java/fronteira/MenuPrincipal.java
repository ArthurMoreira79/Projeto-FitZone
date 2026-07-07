package fronteira;

import java.util.Scanner;

import controle.AdministradorSistema;

public class MenuPrincipal {

    private LeitorEntrada leitor;
    private AdministradorSistema admin;

    /**
     * O Scanner é criado uma única vez aqui e repassado para todos os submenus, evitando múltiplos Scanners abertos sobre o mesmo System.in.
     */
    public MenuPrincipal(AdministradorSistema admin) {
        this.admin = admin;
        this.leitor = new LeitorEntrada(new Scanner(System.in));
    }

    /**
     * Exibe o loop principal do sistema.
     * Lê a opção do usuário e redireciona para o submenu correspondente.
     */
    public void exibir() {
        int opcao = -1;
        while (opcao != 0) {
            ConsoleUtil.titulo("FITZONE - MENU PRINCIPAL");
            System.out.println("1. Alunos");
            System.out.println("2. Ambientes");
            System.out.println("3. Agendamentos e Serviços");
            System.out.println("4. Relatórios");
            System.out.println("0. Sair");
            
            try {
                opcao = leitor.lerInteiro("Escolha: ");
                switch (opcao) {
                    case 1 -> new MenuAlunos(admin, leitor).exibir();
                    case 2 -> new MenuAmbientes(admin, leitor).exibir();
                    case 3 -> new MenuAgendamentos(admin, leitor).exibir();
                    case 4 -> new MenuRelatorios(admin, leitor).exibir();
                    case 0 -> System.out.println("\nEncerrando o sistema...");
                    default -> System.out.println("Opção inválida.");
                }
            } catch (Exception e) {
                System.out.println("Erro inesperado: " + e.getMessage());
            }
        }
    }
}