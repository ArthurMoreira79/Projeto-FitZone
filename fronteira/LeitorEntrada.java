package fronteira;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Centraliza a leitura validada de entradas do usuário, evitando repetir try/catch de parsing em cada classe de menu
 */

public class LeitorEntrada {

    private final Scanner leitor;

    public LeitorEntrada(Scanner leitor) { this.leitor = leitor; }

    /** Lê um inteiro, insistindo até o usuário digitar um valor válido. */
    public int lerInteiro(String prompt) {
        while(true) {
            System.out.println(prompt);
            try {
                return Integer.parseInt(leitor.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido. Digite um numéro inteiro.");
            }
        }
    }
    
    /** Lê uma data no formato AAAA-MM-DD, insistindo até ser válido. */
    public LocalDate lerData(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return LocalDate.parse(leitor.nextLine().trim());
            } catch (DateTimeParseException e) {
                System.out.println("Data inválida. Use o formato AAAA-MM-DD.");
            }
        }
    }

    /** Lê um horário no formato HH:MM, insistindo até ser válido. */
    public LocalTime lerHora(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return LocalTime.parse(leitor.nextLine().trim());
            } catch (DateTimeParseException e) {
                System.out.println("Horário inválido. Use o formato HH:MM.");
            }
        }
    }

    /** Lê um texto não vazio, insistindo até o usuário digitar algo. */
    public String lerTextoObrigatorio(String prompt) {
        while (true) {
            System.out.print(prompt);
            String texto = leitor.nextLine().trim();
            if (!texto.isEmpty()) return texto;
            System.out.println("Este campo não pode ficar vazio.");
        }
    }
}
