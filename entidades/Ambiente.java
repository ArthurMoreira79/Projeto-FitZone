package entidades;

import java.io.Serializable;

public abstract class Ambiente implements Serializable{
    
    private static final long serialVersionUID = 1L;

    private String id;
    private String nome;
    private double valorHora;

    public Ambiente(String id, String nome, double valorHora){
        this.id = id;
        this.nome = nome;
        this.valorHora = valorHora;
    }

    //Getters e Setters

    public String getId()                        { return id; }
    public void setId(String id)                 { this.id = id; }
 
    public String getNome()                      { return nome; }
    public void setNome(String nome)             { this.nome = nome; }
 
    public double getValorHora()                 { return valorHora; }
    public void setValorHora(double valorHora)   { this.valorHora = valorHora; }

    public abstract String getTipo();
    public abstract String getDescricao();
}
