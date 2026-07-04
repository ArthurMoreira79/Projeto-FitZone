package entidades;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


public class Agendamento implements Serializable{
    
    private static final long serialVersionUID = 1L;

    private int id;
    private Aluno aluno;
    private Ambiente ambiente;
    private LocalDate dataAgendamento;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private double valorTotal;
    private List<ServicoAdicional> servicosAdicionais;

    public Agendamento(int id, Aluno aluno, Ambiente ambiente, LocalDate dataAgendamento, LocalTime horaInicio, LocalTime horaFim){
        this.id = id;
        this.aluno = aluno;
        this.ambiente = ambiente;
        this.dataAgendamento = dataAgendamento;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.servicosAdicionais = new ArrayList<>();
        this.valorTotal = calculaValorTotal();
    }


    //Calcula o valor total baseado na duração do agendamento + valor do ambiente; Garante duração mínima de 1 hora para evitar 0.
    private double calculaValorTotal(){
        long horas = Duration.between(horaInicio, horaFim).toHours();
        if(horas <= 0) horas = 1;
        double totalServicos = servicosAdicionais.stream().mapToDouble(ServicoAdicional::getValorTotal).sum();
        return (horas * ambiente.getValorHora()) + totalServicos;
    }

    //Recalcula e atualiza o valor total do agendamento (deve ser chamado após adicionar serviços).
    public void recalcularValorTotal() {
        this.valorTotal = calculaValorTotal();
    }

    public void adicionarServico(ServicoAdicional servico) {
        this.servicosAdicionais.add(servico);
        recalcularValorTotal();
    }

    //Getters e Setters

    public int getId() { return id; }
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
