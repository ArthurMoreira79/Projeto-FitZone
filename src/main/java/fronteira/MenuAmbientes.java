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
                    default -> ConsoleUtil.aviso("Opção inválida.");
                }
            } catch (FalhaPersistenciaException e) {
                ConsoleUtil.erro(e.getMessage());
            } catch (AmbienteJaCadastradoException e) {
                ConsoleUtil.erro(e.getMessage());
            } catch (LimiteAmbienteExcedidoException e) {
                ConsoleUtil.erro(e.getMessage());
            } catch (Exception e) {
                ConsoleUtil.erro("Erro inesperado: " + e.getMessage());
            }
        }
    }

    /**
     * Solicita ao usuário apenas o tipo do ambiente (e uma observação opcional).
     * O ID e o nome são gerados automaticamente pelo AdministradorSistema,
     * dentro da faixa reservada ao tipo escolhido (ver TipoAmbiente).
     */
    private void cadastrar() throws FalhaPersistenciaException, AmbienteJaCadastradoException, LimiteAmbienteExcedidoException {
        ConsoleUtil.subtitulo("CADASTRO DE AMBIENTE");
        System.out.println("Tipos disponíveis:");
        System.out.println("1. Sala de Musculação");
        System.out.println("2. Sala de Yoga");
        System.out.println("3. Sala de Crossfit");
        System.out.println("4. Piscina");
        int opcaoTipo = leitor.lerInteiro("Escolha o tipo: ");

        TipoAmbiente tipo = switch (opcaoTipo) {
            case 1 -> TipoAmbiente.MUSCULACAO;
            case 2 -> TipoAmbiente.YOGA;
            case 3 -> TipoAmbiente.CROSSFIT;
            case 4 -> TipoAmbiente.PISCINA;
            default -> throw new IllegalArgumentException("Tipo de ambiente inválido.");
        };

        String observacoes = leitor.lerTextoOpcional("Observações (opcional, até 200 caracteres): ", 200);

        Ambiente ambiente = admin.cadastrarAmbiente(tipo, observacoes);
        ConsoleUtil.sucesso(ambiente.getNome() + " criado com sucesso!");
        ConsoleUtil.respiro();
        leitor.aguardarContinuar();
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
            System.out.printf("%-20s %-8s %-26s %-10s %-30s%n", "TIPO", "ID", "NOME", "VALOR/H", "OBSERVAÇÕES");
            ConsoleUtil.linha();
            for (Ambiente a : ambientes) {
                String obs = a.getObservacoes();
                if (obs == null) obs = "";
                if (obs.length() > 27) obs = obs.substring(0, 27) + "...";
                System.out.printf("%-20s %-8s %-26s %-10s %-30s%n",
                        a.getTipo(), a.getId(), a.getNome(), String.format("R$ %.2f", a.getValorHora()), obs);
            }
        }
        ConsoleUtil.respiro();
        leitor.aguardarContinuar();
    }
}