package entidades;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Serviço de acompanhamento com personal trainer, com valor fixo por hora
 * de agendamento.
 */

@Entity
@DiscriminatorValue("PERSONAL_TRAINER")
public class PersonalTrainer extends ServicoAdicional{
    
    private static final long serialVersionUID = 1L;

    private static final double VALOR_POR_HORA = 50.0;

    public PersonalTrainer(){}

    @Override
    public String getDescricao() { return "Personal Trainer"; }

    @Override
    public double getValorTotal() { return VALOR_POR_HORA; }

}
