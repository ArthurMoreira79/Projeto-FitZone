package com.fitzone.domain.model;

import java.math.BigDecimal;

/**
 * Centraliza, para cada tipo de {@link Ambiente}, a faixa numérica de IDs
 * reservada a ele (cada tipo tem seu próprio intervalo de 10 números), o nome, o preço e
 * a classe concreta correspondente.
 * 
 * O AmbienteService usa isso para gerar automaticamente o próximo
 * ID disponível de um tipo, sem precisar que o user digite nada.
 * 
 */

public enum TipoAmbiente {

    MUSCULACAO("Sala de Musculação", "100.00", 101, 110, SalaMusculacao.class),
    YOGA("Sala de Yoga", "70.00", 201, 210, SalaYoga.class),
    CROSSFIT("Sala de Crossfit", "100.00", 301, 310, SalaCrossfit.class),
    PISCINA("Piscina", "80.00", 401, 410, Piscina.class);

    private final String nome;
    private final BigDecimal valorHora;
    private final int inicioFaixa;
    private final int fimFaixa;
    private final Class<? extends Ambiente> classe;

    // o preço chega como texto: new BigDecimal("70.00") é exato; new BigDecimal(70.0) herdaria a imprecisão do double
    TipoAmbiente(String nome, String valorHora, int inicioFaixa, int fimFaixa,
                 Class<? extends Ambiente> classe) {
        this.nome = nome;
        this.valorHora = new BigDecimal(valorHora);
        this.inicioFaixa = inicioFaixa;
        this.fimFaixa = fimFaixa;
        this.classe = classe;
    }

    public String getNome() { return nome; }
    public BigDecimal getValorHora() { return valorHora; }
    public int getInicioFaixa() { return inicioFaixa; }
    public int getFimFaixa() { return fimFaixa; }
    public Class<? extends Ambiente> getClasse() { return classe; }
}
