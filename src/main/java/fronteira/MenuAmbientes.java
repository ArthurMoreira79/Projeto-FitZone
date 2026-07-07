package fronteira;

import java.util.List;

import controle.AdministradorSistema;
import entidades.*;
import excecoes.*;

public class MenuAmbientes {

    private LeitorEntrada leitor;
    private AdministradorSistema admin;

    public MenuAmbientes(AdministradorSistema admin, LeitorEntrada leitor) {
        this.admin = admin;
        this.leitor = leitor;
    }

    /**
     * Exibe o submenu de ambientes em loop, com opção de cadastrar e listar, até o usuário voltar.
     * Captura entradas numéricas inválidas sem encerrar o sistema.
     */
    public void exibir() {
        int opcao = -1;
        while (opcao != 0) {
            try {
                ConsoleUtil.titulo("MENU DE AMBIENTES");
                System.out.println("1. Cadastrar novo ambiente");
                System.out.println("2. Listar ambientes cadastrados");
                System.out.println("0. Voltar");
                opcao = leitor.lerInteiro("Escolha: ");

                switch (opcao) {
                    case 1 -> cadastrar();
                    case 2 -> listar();
                    case 0 -> {}
                    default -> System.out.println("Opção inválida.");
                }
            } catch (FalhaPersistenciaException e) {
                System.out.println("Erro ao salvar dados: " + e.getMessage());
            } catch (AmbienteJaCadastradoException e) {
                System.out.println("Erro: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Erro inesperado: " + e.getMessage());
            }
        }
    }

    /**
     * Solicita ao usuário o tipo e o ID do espaço a ser cadastrado.
     * Instancia a subclasse correta de Ambiente via switch e delega o cadastro ao AdministradorSistema.
     */
    private void cadastrar() throws FalhaPersistenciaException, AmbienteJaCadastradoException {
        ConsoleUtil.subtitulo("CADASTRO DE AMBIENTE");
        System.out.println("Tipos disponíveis:");
        System.out.println("1. Sala de Musculação");
        System.out.println("2. Sala de Yoga");
        System.out.println("3. Sala de Crossfit");
        System.out.println("4. Piscina");
        int tipo = leitor.lerInteiro("Escolha o tipo: ");
        String id = leitor.lerTextoObrigatorio("ID do ambiente: ");
        String nome = leitor.lerTextoObrigatorio("Nome do ambiente: ");

        Ambiente ambiente = switch (tipo) {
            case 1 -> new SalaMusculacao(id, nome);
            case 2 -> new SalaYoga(id, nome);
            case 3 -> new SalaCrossfit(id, nome);
            case 4 -> new Piscina(id, nome);
            default -> throw new IllegalArgumentException("Tipo de ambiente inválido.");
        };

        admin.cadastrarAmbiente(ambiente);
        System.out.println("\nAmbiente cadastrado com sucesso!");
        ConsoleUtil.respiro();
    }

    /**
     * Lista todos os ambientes cadastrados no sistema em formato de tabela.
     */
    private void listar() {
        ConsoleUtil.subtitulo("AMBIENTES CADASTRADOS");
        List<Ambiente> ambientes = admin.listarAmbientes();

        if (ambientes.isEmpty()) {
            System.out.println("Nenhum ambiente cadastrado.");
        } else {
            System.out.println();
            System.out.printf("%-20s %-8s %-22s %-10s%n", "TIPO", "ID", "NOME", "VALOR/H");
            ConsoleUtil.linha();
            for (Ambiente a : ambientes) {
                System.out.printf("%-20s %-8s %-22s %-10s%n",
                        a.getTipo(), a.getId(), a.getNome(), String.format("R$ %.2f", a.getValorHora()));
            }
        }
        ConsoleUtil.respiro();
    }
}