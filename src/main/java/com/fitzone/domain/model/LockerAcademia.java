package com.fitzone.domain.model;

import java.math.BigDecimal;

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

    @Column(name = "quantidade")
    private int quantidade;

    protected LockerAcademia() {}

    /**
     * @param quantidade número de lockers contratados (deve ser positivo;
     *                   validada no DTO (Bean Validation) e pela constraint chk_servicos_quantidade do banco)
     */
    public LockerAcademia(int quantidade) { this.quantidade = quantidade; }

    public int getQuantidade() { return quantidade; }

    @Override
    public TipoServico getTipoServico() { return TipoServico.LOCKER; }

    // acrescenta a quantidade ao nome do tipo: "Locker (1 unidade)", "Locker (3 unidades)"
    @Override
    public String getDescricao() {
        return super.getDescricao() + " (" + quantidade + (quantidade == 1 ? " unidade)" : " unidades)");
    }

    @Override
    public BigDecimal getValorTotal() { return getTipoServico().getValor().multiply(BigDecimal.valueOf(quantidade)); }
}
