package com.fitzone.domain.model;

/**
 * Ficha técnica de cada serviço adicional: nome, preço e forma de cobrança.
 * Os nomes das constantes são iguais aos @DiscriminatorValue das subclasses de ServicoAdicional.
 *
 * O cálculo continua polimórfico (cada subclasse sobrescreve getValorTotal()), mas o
 * preço vem daqui: é a única fonte, tanto para a cobrança quanto para a API de tipos.
 */
public enum TipoServico {

    AVALIACAO_FISICA("Avaliação Física", 70.0, Cobranca.FIXO),
    NUTRICIONISTA("Nutricionista", 80.0, Cobranca.FIXO),
    PERSONAL_TRAINER("Personal Trainer", 50.0, Cobranca.POR_HORA),
    LOCKER("Locker", 5.0, Cobranca.POR_UNIDADE);

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
    private final double valor;
    private final Cobranca cobranca;

    TipoServico(String nome, double valor, Cobranca cobranca) {
        this.nome = nome;
        this.valor = valor;
        this.cobranca = cobranca;
    }

    public String getNome() { return nome; }
    public double getValor() { return valor; }
    public Cobranca getCobranca() { return cobranca; }
}
