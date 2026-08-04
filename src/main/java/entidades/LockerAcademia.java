package entidades;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Serviço de aluguel de locker, com valor proporcional à quantidade de
 * unidades contratadas — diferente dos demais serviços, que têm valor fixo.
 */

@Entity
@DiscriminatorValue("LOCKER")
public class LockerAcademia extends ServicoAdicional{
    
    private static final long serialVersionUID = 1L;

    private static final double VALOR_POR_UNIDADE = 5.0;

    @Column(name = "quantidade")
    private int quantidade;

    protected LockerAcademia() {}

    /**
     * @param quantidade número de lockers contratados (deve ser positivo;
     *                   a validação de entrada é feita na camada de fronteira)
     */
    public LockerAcademia(int quantidade) { this.quantidade = quantidade; }

    @Override
    public String getDescricao() { return "Locker (" + quantidade + " unidades)"; }

    @Override
    public double getValorTotal() { return VALOR_POR_UNIDADE * quantidade; }
}
