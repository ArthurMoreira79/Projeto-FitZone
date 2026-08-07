package controle;

import entidades.Ambiente;
import excecoes.FalhaPersistenciaException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;

import java.util.List;

/**
 * Repositório de Ambientes.
 *
 * Fase 2: persistência via JPA/Hibernate em vez de arquivo serializado.
 * Ambiente é uma hierarquia (SalaMusculacao, SalaYoga, SalaCrossfit, Piscina)
 * mapeada com herança SINGLE_TABLE - o Hibernate resolve sozinho qual
 * subclasse instanciar com base na coluna discriminadora "tipo".
 */
public class RepositorioAmbientes {

    /**
     * Insere um novo ambiente no banco.
     */
    public void inserir(Ambiente a) throws FalhaPersistenciaException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(a);
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new FalhaPersistenciaException("Erro ao salvar ambiente no banco de dados.");
        } finally {
            em.close();
        }
    }

    /**
     * Busca e retorna um ambiente pelo id, ou null se não existir.
     */
    public Ambiente buscar(String id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Ambiente.class, id);
        } finally {
            em.close();
        }
    }

    /**
     * Busca o maior ID já cadastrado para um tipo concreto de ambiente
     * (ex.: SalaMusculacao.class), ou null se ainda não existe nenhum
     * desse tipo. Usado pelo AdministradorSistema para calcular o próximo
     * ID disponível dentro da faixa do tipo (ver {@link entidades.TipoAmbiente}).
     *
     * Funciona comparando strings (MAX sobre a coluna id), o que só dá o
     * resultado numericamente correto porque todos os IDs de uma mesma
     * faixa têm sempre 3 dígitos (ex.: "101".."120") - atenção especial
     * se a faixa algum dia crescer além de 999 IDs.
     */
    public String buscarMaiorIdPorTipo(Class<? extends Ambiente> tipo) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT MAX(a.id) FROM Ambiente a WHERE TYPE(a) = :tipo", String.class)
                .setParameter("tipo", tipo)
                .getSingleResult();
        } finally {
            em.close();
        }
    }

    /**
     * Retorna uma lista com todos os ambientes cadastrados.
     */
    public List<Ambiente> listarTodos() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT a FROM Ambiente a", Ambiente.class).getResultList();
        } finally {
            em.close();
        }
    }
}