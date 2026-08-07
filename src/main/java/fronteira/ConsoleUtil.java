package fronteira;

/**
 * Classe utilitaria de apresentação: centraliza separadors, titulos e helpers de formatação usados pelos menus.
 */
public class ConsoleUtil {
    
    private static final int LARGURA = 55;
    private static final String LINHA_DUPLA = "=".repeat(LARGURA);
    private static final String LINHA_SIMPLES = "-".repeat(LARGURA);

    // Códigos ANSI. Terminais modernos (VS Code, Windows Terminal, macOS,
    // Linux) já suportam nativamente. O cmd.exe clássico do Windows mais
    // antigo pode não suportar - nesse caso os códigos aparecem como texto
    // estranho em vez de cor, mas não quebram a execução do programa.
    private static final String RESET   = "\u001B[0m";
    private static final String VERMELHO = "\u001B[31m";
    private static final String VERDE    = "\u001B[32m";
    private static final String AMARELO  = "\u001B[33m";

    private ConsoleUtil() {}

    /**
     * Titulo Principal de uma tela de menu
     */
    public static void titulo(String texto) {
        System.out.println("\n" + LINHA_DUPLA);
        System.out.println(centralizar(texto));
        System.out.println(LINHA_DUPLA);
    }

    /**
     * Subtitulo de uma operação dentro do menu
     */
    public static void subtitulo(String texto) {
        System.out.println("\n-- " + texto + "--");
    }

    /**
     * Linha fina usada pra separar blocos de conteúdo, como linhas de uma tabela ou o fim de uma operação.
     */
    public static void linha() {
        System.out.println(LINHA_SIMPLES);
    }

    /**
     * Espaço em branco extra ao final de uma operação, antes de retornar ao menu.
     */
    public static void respiro() {
        System.out.println();
    }

    /** Mensagem de erro, em vermelho. Use para exceções e entradas inválidas. */
    public static void erro(String texto) {
        System.out.println(VERMELHO + "✘ " + texto + RESET);
    }

    /** Mensagem de sucesso, em verde. Use para confirmar que uma operação deu certo. */
    public static void sucesso(String texto) {
        System.out.println(VERDE + "✔ " + texto + RESET);
    }

    /** Mensagem de aviso, em amarelo. Use para avisos que não são erro, mas merecem atenção. */
    public static void aviso(String texto) {
        System.out.println(AMARELO + "⚠ " + texto + RESET);
    }

    private static String centralizar(String texto) {
        int espacos = Math.max((LARGURA - texto.length()) / 2, 0);
        return " ".repeat(espacos) + texto;
    }
}