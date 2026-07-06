package fronteira;

// Classe utilitaria de apresentação: centraliza separadors, titulos e helpers de formatação usados pelos menus.
public class ConsoleUtil {
    
    private static final int LARGURA = 55;
    private static final String LINHA_DUPLA = "=".repeat(LARGURA);
    private static final String LINHA_SIMPLES = "-".repeat(LARGURA);

    private ConsoleUtil() {}

    //Titulo Principal de uma tela de menu
    public static void titulo(String texto) {
        System.out.println("\n" + LINHA_DUPLA);
        System.out.println(centralizar(texto));
        System.out.println(LINHA_DUPLA);
    }

    //Subtitulo de uma operação dentro do menu
    public static void subtitulo(String texto) {
        System.out.println("\n-- " + texto + "--");
    }

    //Linha fina usada pra separar blocos de conteúdo, como linhas de uma tabela ou o fim de uma operação.
    public static void linha() {
        System.out.println(LINHA_SIMPLES);
    }

    //Espaço em branco extra ao final de uma operação, antes de retornar ao menu.
    public static void respiro() {
        System.out.println();
    }

    private static String centralizar(String texto) {
        int espacos = Math.max((LARGURA - texto.length()) / 2, 0);
        return " ".repeat(espacos) + texto;
    }
}
