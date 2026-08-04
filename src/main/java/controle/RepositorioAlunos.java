package controle;

import entidades.Aluno;
import excecoes.FalhaPersistenciaException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;

import java.util.*;

/**
 * Repositório de Alunos.
 *
 * Fase 2: a persistência deixou de ser em arquivo (.dat serializado) e passou
 * a ser em banco de dados relacional (MySQL) via JPA/Hibernate. As assinaturas
 * dos métodos públicos foram mantidas idênticas às da Fase 1 de propósito -
 * é o Repository Pattern na prática: o AdministradorSistema não precisa saber
 * (nem mudar) como os dados são guardados por baixo dos panos.
 */
public class RepositorioAlunos {

    /**
     * Insere um novo aluno no banco.
     */
    public void inserir(Aluno a) throws FalhaPersistenciaException{
        EntityManager em = JPAUtil.getEntityManager();
        try{
            em.getTransaction().begin();
            em.persist(a);
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            if(em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new FalhaPersistenciaException("Erro ao salvar aluno no banco de dados.");
        } finally {
            em.close();
        }
    }

    /**
     * Busca e retorna um aluno pelo cpf, ou null se não existir.
     */
    public Aluno buscar(String cpf) {
        EntityManager em = JPAUtil.getEntityManager();
        try{
            return em.find(Aluno.class, cpf);
        } finally{
            em.close();
        }
    }

    /**
     * Retorna uma lista com todos os alunos cadastrados.
     */
    public List<Aluno> listarTodos(){
        EntityManager em = JPAUtil.getEntityManager();
        try{
            return em.createQuery("SELECT a FROM Aluno a", Aluno.class).getResultList();
        } finally{
            em.close();
        }
    }
}
