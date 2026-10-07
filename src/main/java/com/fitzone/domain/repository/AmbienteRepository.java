package com.fitzone.domain.repository;

import com.fitzone.domain.model.Ambiente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AmbienteRepository extends JpaRepository <Ambiente, Integer> {
    
    /**
     * Maior id já usado por um tipo de ambiente, ou null se ainda não houver nenhum
     * TYPE(a) filtra pela subclasse na herança SINGLE_TABLE(coluna "tipo")
     * Usado pelo AmbienteService para gerar o próximo id dentro da faixa do TipoAmbiente.
     */
    @Query("SELECT MAX(a.id) FROM Ambiente a WHERE TYPE(a) = :tipo")
    Integer buscarMaiorIdPorTipo(@Param("tipo") Class<? extends Ambiente> tipo);
}
