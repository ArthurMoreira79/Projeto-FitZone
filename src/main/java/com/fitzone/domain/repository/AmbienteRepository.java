package com.fitzone.domain.repository;

import com.fitzone.domain.model.Ambiente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface AmbienteRepository extends JpaRepository <Ambiente, Integer> {

    /**
     * Ids já usados por um tipo de ambiente (só a coluna id, sem carregar as entidades).
     * TYPE(a) filtra pela subclasse na herança SINGLE_TABLE (coluna "tipo").
     * Usado pelo AmbienteService para achar o primeiro id livre na faixa do TipoAmbiente.
     */
    @Query("SELECT a.id FROM Ambiente a WHERE TYPE(a) = :tipo")
    Set<Integer> buscarIdsPorTipo(@Param("tipo") Class<? extends Ambiente> tipo);
}
