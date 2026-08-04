package entidades;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Serviço de avaliação física, com valor fixo por sessão (não varia por
 * quantidade ou duração).
 */

@Entity
@DiscriminatorValue("AVALIACAO_FISICA")
public class AvaliacaoFisica extends ServicoAdicional{
    
    private static final long serialVersionUID = 1L;

    private static final double VALOR_AVALIACAO = 70.0;

    public AvaliacaoFisica(){}

    @Override
    public String getDescricao() { return "Avaliação Física"; }

    @Override
    public double getValorTotal() { return VALOR_AVALIACAO; }
}
