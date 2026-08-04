package entidades;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serializable;

/**
 * Contrato comum para todos os serviços adicionais que podem ser vinculados
 * a um {@link Agendamento} (avaliação física, nutricionista, personal trainer,
 * locker etc.). Cada implementação decide sua própria descrição e forma de
 * cálculo do valor cobrado.
 * 
 * OBS (FASE 2): esta classe era uma interface na fase 1. Como o JPA/Hibernate
 * só consegue mapear classes como entidades (não interfaces), ela foi convertida
 * em classe abstrata com herança SINGLE_TABLE, igual foi feito em Ambiente.
 * O polimorfismo continua real: cada subtipo ainda sobrescreve getDescricao()
 * e getValorTotal() com sua própria regra.
 */

@Entity
@Table(name = "servicos_adicionais")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING, length = 30)
public abstract class ServicoAdicional implements Serializable {
   
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agendamento_id")
    private Agendamento agendamento;

    protected ServicoAdicional() {}

    public Long getId() { return id; }

    public Agendamento getAgendamento() { return agendamento; }
    public void setAgendamento(Agendamento agendamento) { this.agendamento = agendamento; }

    /**
     * @return uma descrição curta e legível do serviço, usada em listagens e recibos
     */
    public abstract String getDescricao();

    /**
     * @return o valor total a ser cobrado por esse serviço, já considerando
     *         quantidade ou duração quando aplicável (ex.: locker por unidade)
     */
    public abstract double getValorTotal();
}
