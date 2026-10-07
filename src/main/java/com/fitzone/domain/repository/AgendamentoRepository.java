package com.fitzone.domain.repository;

import com.fitzone.domain.model.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository <Agendamento, Integer> {
    
    /**
     * Agendamentos do mesmo ambiente, no mesmo dia, cujo horário se sobrepõe a [inicio, fim].  
     * Dois intervalos se sobrepõem quando um começa antes do outro terminar, nos dois sentidos.
     * Horários "encostados" (8h-9h e 9h-10h) NÃO conflitam. 
     * Usa o índice idx_agendamentos_conglito, sem varrer a tabela inteira.    
     */

    @Query("SELECT a FROM Agendamento a " +
           "WHERE a.ambiente.id = :ambienteId " +
           "AND a.dataAgendamento = :data " +
           "AND a.horaInicio < :fim " +
           "AND a.horaFim > :inicio")
    List<Agendamento> buscarConflitos(@Param("ambienteId") Integer ambienteId,
                                      @Param("data") LocalDate data,
                                      @Param("inicio") LocalTime inicio,
                                      @Param("fim") LocalTime fim);
    
    //Relatório por aluno: filtra no banco em vez de findAll() + filtro em memória.  
    //"Aluno_ID" navega do atributo aluno para o id dele
    List<Agendamento> findByAluno_IdOrderByDataAgendamentoAscHoraInicioAsc(Integer alunoId);                                 
}
