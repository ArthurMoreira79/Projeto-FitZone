package repository;

import entidades.Ambiente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AmbienteRepository extends JpaRepository<Ambiente, String> {

    @Query("SELECT MAX(a.id) FROM Ambiente a WHERE TYPE(a) = :tipo")
    String buscarMaiorIdPorTipo(@Param("tipo") Class<? extends Ambiente> tipo);
}
