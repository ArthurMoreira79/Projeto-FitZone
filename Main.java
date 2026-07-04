import controle.*;
import fronteira.*;
import excecoes.*;

public class Main {

    public static void main(String[] args) {
        System.out.println("===== SISTEMA FITZONE =====");
        System.out.println("Status: Inicializando módulos...");

        try {
            AdministradorSistema admin = new AdministradorSistema();
            MenuPrincipal menu = new MenuPrincipal(admin);
            menu.exibir();
 
        } catch (FalhaPersistenciaException e) {
            System.err.println("ERRO DE ARQUIVO: O sistema não conseguiu carregar os dados salvos.");
            System.err.println("Detalhe: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("ERRO DE FORMATO: Foi inserido um caractere inválido em um campo numérico.");
        } catch (NullPointerException e) {
            System.err.println("ERRO INTERNO: Tentativa de acessar um dado inexistente (NullPointer).");
        } catch (Exception e) {
            System.err.println("ERRO INESPERADO: Ocorreu uma falha não mapeada no sistema.");
            System.err.println("Causa: " + e.getMessage());
        } finally {
            System.out.println("\n-------------------------------------------");
            System.out.println("Sistema FitZone encerrado.");
            System.out.println("Todos os logs foram registrados com segurança.");
        }
    }
}