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
            System.out.print(prompt);
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

    /** Lê um inteiro estritamente positivo (> 0), insistindo até ser válido. */
    public int lerInteiroPositivo(String prompt) {
        while (true) {
            int valor = lerInteiro(prompt);
            if (valor > 0) return valor;
            System.out.println("A quantidade deve ser maior que zero.");
        }
    }

    /**
     * Lê um CPF, insistindo até que contenha exatamente 11 dígitos numéricos.
     * Aceita pontos/traço na digitação e os remove antes de validar
     * regra de formato de entrado, não confundir com validação de dígito verificador.
     */
    public String lerCpf(String prompt) {
        while(true) {
            String cpf = lerTextoObrigatorio(prompt).replaceAll("[.\\-\\s]", "");
            if (cpf.matches("\\d{11}")) return cpf;
            System.out.println("CPF inválido. Digite 11 dígitos numéricos.");
        }
    }

    /** Lê uma data válida que não pode ser anterior a hoje (permite hoje ou datas futuras). */
    public LocalDate lerDataNaoAnterior(String prompt) {
        while(true) {
            LocalDate data = lerData(prompt);
            if (!data.isBefore(LocalDate.now())) return data;
            System.out.println("A data não pode ser no passado. Informe a data de hoje ou uma data futura.");
        }
    }

    /** Lê um horário válido que precisa ser estritamente posterior a uma referência. */
    public LocalTime lerHoraApos(String prompt, LocalTime referencia) {
        while (true) {
            LocalTime hora = lerHora(prompt);
            if (!hora.isAfter(referencia)) return hora;
            System.out.println("O horário finald deve ser depois do horário de início (" + referencia + ").");
        }
    }
}
