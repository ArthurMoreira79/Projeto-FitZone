package entidades;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

import java.io.Serializable;

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
public abstract class Ambiente implements Serializable{
    
    private static final long serialVersionUID = 1L;

    @Id
    @Column(length = 10)
    private String id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(name = "valor_hora", nullable = false)
    private double valorHora;

    protected Ambiente() {}

    public Ambiente(String id, String nome, double valorHora){
        this.id = id;
        this.nome = nome;
        this.valorHora = valorHora;
    }

    /* Getters e Setters */

    public String getId()                        { return id; }
    public void setId(String id)                 { this.id = id; }
 
    public String getNome()                      { return nome; }
    public void setNome(String nome)             { this.nome = nome; }
 
    public double getValorHora()                 { return valorHora; }
    public void setValorHora(double valorHora)   { this.valorHora = valorHora; }

    public abstract String getTipo();
    public abstract String getDescricao();
}
