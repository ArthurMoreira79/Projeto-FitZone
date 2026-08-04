package entidades;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Serviço de consulta com nutricionista, com valor fixo por consulta.
 */

@Entity
@DiscriminatorValue("NUTRICIONISTA")
public class Nutricionista extends ServicoAdicional{
    
    private static final long serialVersionUID = 1L;

    private static final double VALOR_CONSULTA = 80.0;

    public Nutricionista(){}

    @Override
    public String getDescricao() { return "Nutricionista"; }

    @Override
    public double getValorTotal() { return VALOR_CONSULTA; }
}
