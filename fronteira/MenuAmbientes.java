package fronteira;

import java.util.List;
import java.util.Scanner;
import controle.AdministradorSistema;
import excecoes.*;
import entidades.*;

public class MenuAmbientes {

    private Scanner leitor;
    private AdministradorSistema admin;

    public MenuAmbientes(AdministradorSistema admin, Scanner leitor) {
        this.admin = admin;
        this.leitor = leitor;
    }

    //Exibe o submenu de ambientes em loop, com opção de cadastrar e listar, até o usuário voltar.
    //Captura entradas numéricas inválidas sem encerrar o sistema.
    public void exibir() {
        int opcao = -1;
        while (opcao != 0) {
            try {
                System.out.println("\n===== MENU DE AMBIENTES =====");
                System.out.println("1. Cadastrar novo ambiente");
                System.out.println("2. Listar ambientes cadastrados");
                System.out.println("0. Voltar");
                System.out.print("Escolha: ");

                opcao = Integer.parseInt(leitor.nextLine());

                switch (opcao) {
                    case 1 -> cadastrar();
                    case 2 -> listar();
                    case 0 -> {} //volta ao menu principal
                    default -> System.out.println("Opção inválida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Erro: Valor numérico inválido.");
            } catch (FalhaPersistenciaException e) {
                System.out.println("Erro ao salvar dados: " + e.getMessage());
            } catch (AmbienteIndisponivelException e) {
                System.out.println("Erro: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Erro inesperado: " + e.getMessage());
            }
        }
    }

    //Solicita ao usuário o tipo e o ID do espaço a ser cadastrado. Instancia a subclasse
    //correta de Ambiente via switch e delega o cadastro ao AdministradorSistema.
    private void cadastrar() throws FalhaPersistenciaException, AmbienteIndisponivelException {
        System.out.println("\n--- CADASTRO DE AMBIENTE ---");
        System.out.println("Tipos disponíveis:");
        System.out.println("1. Sala de Musculação");
        System.out.println("2. Sala de Yoga");
        System.out.println("3. Sala de Crossfit");
        System.out.println("4. Piscina");
        System.out.print("Escolha o tipo: ");

        int tipo = Integer.parseInt(leitor.nextLine());
        System.out.print("ID do ambiente: ");
        String id = leitor.nextLine();
        System.out.print("Nome do ambiente: ");
        String nome = leitor.nextLine();

        Ambiente ambiente = switch (tipo) {
            case 1 -> new SalaMusculacao(id, nome);
            case 2 -> new SalaYoga(id, nome);
            case 3 -> new SalaCrossfit(id, nome);
            case 4 -> new Piscina(id, nome);
            default -> throw new IllegalArgumentException("Tipo de ambiente inválido.");
        };

        admin.cadastrarAmbiente(ambiente);
        System.out.println("Ambiente cadastrado com sucesso!");
    }

    //Lista todos os ambientes cadastrados no sistema, exibindo suas informações de forma organizada.
    private void listar() {
        System.out.println("\n--- AMBIENTES CADASTRADOS ---");
        List<Ambiente> ambientes = admin.listarAmbientes();

        if (ambientes.isEmpty()) {
            System.out.println("Nenhum ambiente cadastrado.");
        } else {
            for (Ambiente a : ambientes) {
                System.out.println(a.getDescricao());
            }
        }
    }
}