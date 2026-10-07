package com.fitzone.domain.model;

/**
 * Centraliza, para cada tipo de {@link Ambiente}, a faixa numérica de IDs
 * reservada a ele (cada tipo tem seu próprio intervalo de 10 números) e 
 * a classe concreta correspondente.
 * 
 * O AmbienteService usa isso para gerar automaticamente o próximo
 * ID disponível de um tipo, sem precisar que o user digite nada.
 * 
 */

public enum TipoAmbiente {

    MUSCULACAO(101, 110, SalaMusculacao.class),
    YOGA(201, 210, SalaYoga.class),
    CROSSFIT(301, 310, SalaCrossfit.class),
    PISCINA(401, 410, Piscina.class);

    private final int inicioFaixa;
    private final int fimFaixa;
    private final Class<? extends Ambiente> classe;

    TipoAmbiente(int inicioFaixa, int fimFaixa, Class<? extends Ambiente> classe) {
        this.inicioFaixa = inicioFaixa;
        this.fimFaixa = fimFaixa;
        this.classe = classe;
    }

    public int getInicioFaixa() { return inicioFaixa; }
    public int getFimFaixa() { return fimFaixa; }
    public Class<? extends Ambiente> getClasse() { return classe; }
    
}
