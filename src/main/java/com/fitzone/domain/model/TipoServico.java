package com.fitzone.domain.model;

import java.math.BigDecimal;

/**
 * Ficha técnica de cada serviço adicional: nome, preço e forma de cobrança.
 * Os nomes das constantes são iguais aos @DiscriminatorValue das subclasses de ServicoAdicional.
 *
 * O cálculo continua polimórfico (cada subclasse sobrescreve getValorTotal()), mas o
 * preço vem daqui: é a única fonte, tanto para a cobrança quanto para a API de tipos.
 */
public enum TipoServico {

    AVALIACAO_FISICA("Avaliação Física", "70.00", Cobranca.FIXO),
    NUTRICIONISTA("Nutricionista", "80.00", Cobranca.FIXO),
    PERSONAL_TRAINER("Personal Trainer", "50.00", Cobranca.POR_HORA),
    LOCKER("Locker", "5.00", Cobranca.POR_UNIDADE);

    /** Como o preço vira valor cobrado. */
    public enum Cobranca {
        /** valor único por agendamento */
        FIXO,
        /** preço × horas cobradas do agendamento (Agendamento.getHorasCobradas()) */
        POR_HORA,
        /** preço × quantidade contratada */
        POR_UNIDADE
    }

    private final String nome;
    private final BigDecimal valor;
    private final Cobranca cobranca;

    // preço como texto, pelo mesmo motivo do TipoAmbiente
    TipoServico(String nome, String valor, Cobranca cobranca) {
        this.nome = nome;
        this.valor = new BigDecimal(valor);
        this.cobranca = cobranca;
    }

    public String getNome() { return nome; }
    public BigDecimal getValor() { return valor; }
    public Cobranca getCobranca() { return cobranca; }
}
