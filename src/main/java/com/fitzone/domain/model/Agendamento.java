package com.fitzone.domain.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Entity
@Table(name = "agendamentos")
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ambiente_id", nullable = false)
    private Ambiente ambiente;

    @Column(name = "data_agendamento", nullable = false)
    private LocalDate dataAgendamento;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fim", nullable = false)
    private LocalTime horaFim;

    @Column(name = "valor_total", nullable = false)
    private double valorTotal;

    @OneToMany(mappedBy = "agendamento", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ServicoAdicional> servicosAdicionais;

    protected Agendamento() { } // exigido pelo JPA

    public Agendamento(Aluno aluno, Ambiente ambiente, LocalDate dataAgendamento, LocalTime horaInicio, LocalTime horaFim){
        this.aluno = aluno;
        this.ambiente = ambiente;
        this.dataAgendamento = dataAgendamento;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.servicosAdicionais = new ArrayList<>();
        this.valorTotal = calculaValorTotal();
    }


    /**
     * Horas cobradas do agendamento: só contam horas completas (2h40 → 2h; a
     * hora seguinte só é cobrada se for completada), com mínimo de 1 hora.
     * Esta é a ÚNICA fonte da regra de duração - ambiente, Personal Trainer e
     * relatórios devem usar este método, nunca recalcular por conta própria.
     */
    public long getHorasCobradas() {
        long horas = Duration.between(horaInicio, horaFim).toHours();
        return Math.max(horas, 1);
    }

    /**
     * Valor total = horas cobradas × valor/hora do ambiente + soma dos serviços adicionais.
     */
    private double calculaValorTotal(){
        double totalServicos = servicosAdicionais.stream().mapToDouble(ServicoAdicional::getValorTotal).sum();
        return (getHorasCobradas() * ambiente.getValorHora()) + totalServicos;
    }

    /**
     * Recalcula e atualiza o valor total do agendamento (deve ser chamado após adicionar serviços).
     */
    public void recalcularValorTotal() {
        this.valorTotal = calculaValorTotal();
    }

    public void adicionarServico(ServicoAdicional servico) {
        servico.setAgendamento(this);
        this.servicosAdicionais.add(servico);
        recalcularValorTotal();
    }

    /** Getters e Setters */

    /**
     * Retorna o ID do agendamento. Antes de o agendamento ser persistido
     * (INSERT no banco), o Hibernate ainda não gerou o valor, então retorna 0
     * (igual ao comportamento antigo com int primitivo) em vez de lançar NPE.
     */
    public Integer getId() { return id; }
    public Aluno getAluno() { return aluno; }
    public Ambiente getAmbiente() { return ambiente; }
    public LocalDate getDataAgendamento() { return dataAgendamento; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public List<ServicoAdicional> getServicosAdicionais() { return servicosAdicionais; }

    public double getValorTotal() { return valorTotal; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }

    @Override
    public String toString(){
        return "Agendamento #" + id
                 + " | Aluno: " + aluno.getNome()
                 + " | Ambiente: " + ambiente.getNome()
                 + " | Data: " + dataAgendamento
                 + " | " + horaInicio + " - " + horaFim
                 + " | Total: R$" + String.format("%.2f", valorTotal);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Agendamento outro)) return false;
        return id != null && Objects.equals(id, outro.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}