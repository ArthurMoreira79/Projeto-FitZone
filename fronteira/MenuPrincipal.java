package fronteira;

import java.util.Scanner;
import controle.AdministradorSistema;

public class MenuPrincipal {
    
    private Scanner leitor;
    private AdministradorSistema admin;

    public MenuPrincipal(AdministradorSistema admin) { this.admin = admin; this.leitor = new Scanner(System.in); }

    //Exibe o loop principal do sistema. Lê a opção do user e redireciona para o submenu corespondente.

    public void exibir() { 
        int opcao = -1;
        while (opcao != 0) {
            System.out.println("\n===== FITZONE - MENU PRINCIPAL =====");
            System.out.println("1. Alunos");
            System.out.println("2. Ambientes");
            System.out.println("3. Agendamentos e Serviços");
            System.out.println("4. Relatórios");
            System.out.println("0. Sair");
            System.out.print("Escolha: ");
            try{
                opcao = Integer.parseInt(leitor.nextLine());
                switch(opcao){
                    case 1 -> new MenuAlunos(admin, leitor).exibir();
                    case 2 -> new MenuAmbientes(admin, leitor).exibir();
                    case 3 -> new MenuAgendamentos(admin, leitor).exibir();
                    case 4 -> new MenuRelatorios(admin, leitor).exibir();
                    case 0 -> System.out.println("Encerrando...");
                    default -> System.out.println("Opção inválida.");
                }
            } catch(Exception e){
                System.out.println("Erro: Entrada inválida.");
            }
        }
    }
}
