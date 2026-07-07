package fronteira;

import java.util.List;
import java.time.LocalDate;
import entidades.Aluno;
import controle.AdministradorSistema;
import excecoes.*;

public class MenuAlunos {

    private LeitorEntrada leitor;
    private AdministradorSistema admin;

    public MenuAlunos(AdministradorSistema admin, LeitorEntrada leitor) {
        this.admin = admin;
        this.leitor = leitor;
    }

    /**
     * Exibe o submenu de alunos em loop, com opções de cadastrar, buscar e listar, até o usuário escolher voltar ao menu principal.
     */
    public void exibir() {
        int opcao = -1;
        while (opcao != 0) {
            try {
                ConsoleUtil.titulo("MENU DE ALUNOS");
                System.out.println("1. Cadastrar novo aluno");
                System.out.println("2. Buscar aluno por CPF");
                System.out.println("3. Listar todos os alunos");
                System.out.println("0. Voltar");
                opcao = leitor.lerInteiro("Escolha: ");

                switch (opcao) {
                    case 1 -> cadastrar();
                    case 2 -> buscar();
                    case 3 -> listar();
                    case 0 -> {} /* volta ao menu principal */
                    default -> System.out.println("Opção inválida.");
                }
            } catch (AlunoJaCadastradoException | AlunoNaoEncontradoException e) {
                System.out.println("Erro: " + e.getMessage());
            } catch (FalhaPersistenciaException e) {
                System.out.println("Erro ao salvar dados: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Erro inesperado: " + e.getMessage());
            }
        }
    }

    /**
     * Solicita os dados do novo aluno e delega o cadastro ao AdministradorSistema.
     */
    private void cadastrar() throws AlunoJaCadastradoException, FalhaPersistenciaException {
        ConsoleUtil.subtitulo("CADASTRO DE ALUNO");
        String cpf = leitor.lerCpf("CPF (11 dígitos): ");
        String nome = leitor.lerTextoObrigatorio("Nome: ");
        String email = leitor.lerTextoObrigatorio("Email: ");
        String telefone = leitor.lerTextoObrigatorio("Telefone: ");

        admin.cadastrarAluno(new Aluno(cpf, nome, email, telefone, LocalDate.now()));
        System.out.println("\nAluno cadastrado com sucesso!");
        ConsoleUtil.respiro();
    }

    /**
     * Busca um aluno pelo CPF e exibe seus dados. Lança AlunoNaoEncontradoException se não existir.
     */
    private void buscar() throws AlunoNaoEncontradoException {
        ConsoleUtil.subtitulo("BUSCAR ALUNO");
        String cpf = leitor.lerCpf("CPF (11 dígitos): ");

        Aluno aluno = admin.buscarAluno(cpf);
        System.out.println();
        imprimirCabecalho();
        imprimirLinha(aluno);
        ConsoleUtil.respiro();
    }

    /**
     * Lista todos os alunos cadastrados no sistema em formato de tabela.
     */
    private void listar() {
        ConsoleUtil.subtitulo("ALUNOS CADASTRADOS");
        List<Aluno> alunos = admin.listarAlunos();

        if (alunos.isEmpty()) {
            System.out.println("Nenhum aluno cadastrado.");
        } else {
            System.out.println();
            imprimirCabecalho();
            alunos.forEach(this::imprimirLinha);
        }
        ConsoleUtil.respiro();
    }

    private void imprimirCabecalho() {
        System.out.printf("%-15s %-22s %-25s %-15s %-12s%n", "CPF", "NOME", "EMAIL", "TELEFONE", "CADASTRO");
        ConsoleUtil.linha();
    }

    private void imprimirLinha(Aluno a) {
        System.out.printf("%-15s %-22s %-25s %-15s %-12s%n",
                a.getCpf(), a.getNome(), a.getEmail(), a.getTelefone(), a.getDataCadastro());
    }
}