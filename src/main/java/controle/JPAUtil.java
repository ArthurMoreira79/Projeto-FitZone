package controle;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Fábrica única de {@link EntityManager} para toda a aplicação (padrão Singleton).
 * Centraliza a configuração JPA lida de META-INF/persistence.xml
 * (persistence-unit "fitzonePU").
 *
 * O {@link EntityManagerFactory} é caro de criar (abre e valida a conexão com
 * o MySQL, lê o mapeamento de todas as entidades), então é criado uma única
 * vez e reaproveitado. Já o {@link EntityManager} é leve e "descartável":
 * cada operação de repositório abre o seu, usa e fecha.
 */

public final class JPAUtil {

    private static final EntityManagerFactory EMF = Persistence.createEntityManagerFactory("fitzonePU");

    private JPAUtil() {}

    /**
     * Abre o novo EM. Quem chamar é responsável por fecha-lo (idealmente em um bloco try/finally).
     */
    public static EntityManager getEntityManager() {
        return EMF.createEntityManager();
    }

    /**
     * Encerra a fábrica de conexões. Deve ser chamado apenas uma vez, ao finalizar a aplicação(ver Main.java).
     */
    public static void fechar(){
        if(EMF.isOpen()){
            EMF.close();
        }
    }
    
}
