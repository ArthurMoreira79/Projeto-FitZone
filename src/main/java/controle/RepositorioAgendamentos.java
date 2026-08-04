package controle;

import entidades.Agendamento;
import excecoes.FalhaPersistenciaException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;

import java.util.*;

/**
 * Repositório de Agendamentos.
 *
 * Fase 2: persistência via JPA/Hibernate. O antigo contador manual de ID
 * (gerarProximoId()) deixou de existir aqui - o MySQL agora gera o ID
 * automaticamente (AUTO_INCREMENT) no momento do INSERT, e o Hibernate
 * já devolve esse valor preenchido no próprio objeto Agendamento.
 */
public class RepositorioAgendamentos {

    /**
     * Insere um novo agendamento no banco(em cascade, também insere os serviços adicionais
     * já vinculados a ele, graças ao CascadeType.ALL mapeado em Agendamentos.servicosAdicionais).
     */
    public void inserir(Agendamento a) throws FalhaPersistenciaException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(a);
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new FalhaPersistenciaException("Erro ao salvar agendamento no banco de dados.");
        } finally {
            em.close();
        }
    }

    /**
     * Remove um agendamento do banco pelo ID (e, em cascata, seus serviços adicionais, graças 
     * ao orphanRemoval/ON DELETE CASCADE).
     */
    public void remover(int id) throws FalhaPersistenciaException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Agendamento a = em.find(Agendamento.class, id);
            if (a != null) em.remove(a);
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new FalhaPersistenciaException("Erro ao remover agendamento no banco de dados.");
        } finally {
            em.close();
        }
    }

    /**
     * Atualiza um agendamento existente no banco (por exemplo, após adicionar um novo serviço a ele).
     */
    public void atualizar(Agendamento a) throws FalhaPersistenciaException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(a);
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new FalhaPersistenciaException("Erro ao atualizar agendamento no banco de dados.");
        } finally {
            em.close();
        }
    }

    /**
     * Busca e retorna um agendamento pelo ID, ou null se não existir.
     */
    public Agendamento buscar(int id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Agendamento.class, id);
        } finally{
            em.close();
        }
    }

    /**
     * Retorna uma lista com todos os agendamentos cadastrados.
     */
    public List<Agendamento> listarTodos(){
        EntityManager em = JPAUtil.getEntityManager();
        try{
            return em.createQuery("SELECT a FROM Agendamento a", Agendamento.class).getResultList();
        } finally{
            em.close();
        }
    }
}
