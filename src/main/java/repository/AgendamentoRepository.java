package repository;

import entidades.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Integer> {

    @Query("SELECT a FROM Agendamento a " +
           "WHERE a.ambiente.id = :ambienteId " +
           "AND a.dataAgendamento = :data " +
           "AND a.horaInicio < :fim " +
           "AND a.horaFim > :inicio")
    List<Agendamento> buscarConflitos(@Param("ambienteId") String ambienteId,
                                       @Param("data") LocalDate data,
                                       @Param("inicio") LocalTime inicio,
                                       @Param("fim") LocalTime fim);
}
