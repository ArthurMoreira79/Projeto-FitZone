package fronteira;

import java.util.List;
import java.util.Scanner;
import java.time.LocalDate;
import entidades.Aluno;
import controle.AdministradorSistema;
import excecoes.*;

public class MenuAlunos {

    private Scanner leitor;
    private AdministradorSistema admin;

    public MenuAlunos(AdministradorSistema admin, Scanner leitor) {
        this.admin = admin;
        this.leitor = leitor;
    }

    //Exibe o submenu de alunos em loop, com opções de cadastrar, buscar e listar,
    //até o usuário escolher voltar ao menu principal.
    public void exibir() {
        int opcao = -1;
        while (opcao != 0) {
            try {
                System.out.println("\n===== MENU DE ALUNOS =====");
                System.out.println("1. Cadastrar novo aluno");
                System.out.println("2. Buscar aluno por CPF");
                System.out.println("3. Listar todos os alunos");
                System.out.println("0. Voltar");
                System.out.print("Escolha: ");

                opcao = Integer.parseInt(leitor.nextLine());

                switch (opcao) {
                    case 1 -> cadastrar();
                    case 2 -> buscar();
                    case 3 -> listar();
                    case 0 -> {} //volta ao menu principal
                    default -> System.out.println("Opção inválida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Erro: Entrada numérica inválida.");
            } catch (AlunoJaCadastradoException | AlunoNaoEncontradoException e) {
                System.out.println("Erro: " + e.getMessage());
            } catch (FalhaPersistenciaException e) {
                System.out.println("Erro ao salvar dados: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Erro inesperado: " + e.getMessage());
            }
        }
    }

    //Solicita os dados do novo aluno e delega o cadastro ao AdministradorSistema.
    private void cadastrar() throws AlunoJaCadastradoException, FalhaPersistenciaException {
        System.out.println("\n--- CADASTRO DE ALUNO ---");
        System.out.print("CPF: ");        String cpf = leitor.nextLine();
        System.out.print("Nome: ");       String nome = leitor.nextLine();
        System.out.print("Email: ");      String email = leitor.nextLine();
        System.out.print("Telefone: ");   String telefone = leitor.nextLine();

        admin.cadastrarAluno(new Aluno(cpf, nome, email, telefone, LocalDate.now()));
        System.out.println("Aluno cadastrado com sucesso!");
    }

    //Busca um aluno pelo CPF e exibe seus dados. Lança AlunoNaoEncontradoException se não existir.
    private void buscar() throws AlunoNaoEncontradoException {
        System.out.println("\n--- BUSCAR ALUNO ---");
        System.out.print("CPF: ");
        String cpf = leitor.nextLine();

        Aluno aluno = admin.buscarAluno(cpf);
        System.out.println(aluno);
    }

    //Lista todos os alunos cadastrados no sistema.
    private void listar() {
        System.out.println("\n--- ALUNOS CADASTRADOS ---");
        List<Aluno> alunos = admin.listarAlunos();

        if (alunos.isEmpty()) {
            System.out.println("Nenhum aluno cadastrado.");
        } else {
            alunos.forEach(System.out::println);
        }
    }
}