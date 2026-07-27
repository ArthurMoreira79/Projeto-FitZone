package entidades;

import java.io.Serializable;

/**
 * Contrato comum para todos os serviços adicionais que podem ser vinculados
 * a um {@link Agendamento} (avaliação física, nutricionista, personal trainer,
 * locker etc.). Cada implementação decide sua própria descrição e forma de
 * cálculo do valor cobrado.
 */

public interface ServicoAdicional extends Serializable {
    
    /**
     * @return uma descrição curta e legível do serviço, usada em listagens e recibos
     */
    String getDescricao();

    /**
     * @return o valor total a ser cobrado por esse serviço, já considerando
     *         quantidade ou duração quando aplicável (ex.: locker por unidade)
     */
    double getValorTotal();
}
