package entidades;

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

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "agendamentos")
public class Agendamento implements Serializable{
    
    private static final long serialVersionUID = 1L;

    /*
     * Fase 2: o ID deixou de ser gerado manualmente (RepositorioAgendamentos.gerarProximoId())
     * e passou a ser AUTO_INCREMENT no MySQL. Por isso é Integer (aceita null antes do
     * INSERT) em vez de int primitivo, e não existe mais no construtor de negócio -
     * o Hibernate preenche esse campo sozinho assim que o objeto é persistido.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "aluno_cpf", nullable = false)
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
     * Calcula o valor total baseado na duração do agendamento + valor do ambiente.
     * Garante duração mínima de 1 hora para evitar 0.
     */
    private double calculaValorTotal(){
        long horas = Duration.between(horaInicio, horaFim).toHours();
        if(horas <= 0) horas = 1;
        double totalServicos = servicosAdicionais.stream().mapToDouble(ServicoAdicional::getValorTotal).sum();
        return (horas * ambiente.getValorHora()) + totalServicos;
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
    public int getId() { return id != null ? id : 0; }
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
}