package com.fitzone.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

import java.util.Objects;

/**
 * Representa um ambiente reservável do estúdio (salas, piscina, etc.).
 * Classe abstrata: cada tipo concreto define sua própria descrição.
 * 
 * Mapeada com herança SINGLE_TABLE: todos os subtipos vivem na mesma tabela
 * "ambientes", diferenciados pela coluna discriminadora "tipo".
*/

@Entity
@Table(name = "ambientes")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING, length = 20)
public abstract class Ambiente {

    // o id NÃO usa @GeneratedValue: é calculado pela faixa do TipoAmbiente (ex.: 101-110)
    @Id
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(name = "valor_hora", nullable = false)
    private double valorHora;

    @Column(name = "observacoes", length = 200)
    private String observacoes;

    protected Ambiente() {}

    public Ambiente(Integer id, String nome, double valorHora, String observacoes){
        this.id = id;
        this.nome = nome;
        this.valorHora = valorHora;
        this.observacoes = observacoes;
    }

    /* Getters e Setters */

    public Integer getId()                       { return id; }
    public void setId(Integer id)                { this.id = id; }
 
    public String getNome()                      { return nome; }
    public void setNome(String nome)             { this.nome = nome; }
 
    public double getValorHora()                 { return valorHora; }
    public void setValorHora(double valorHora)   { this.valorHora = valorHora; }

    /** Descrição livre e opcional digitada pelo usuário no cadastro (até 200 caracteres). */
    public String getObservacoes()               { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public abstract String getTipo();
    public abstract String getDescricao();

        @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ambiente outro)) return false;
        return id != null && Objects.equals(id, outro.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
