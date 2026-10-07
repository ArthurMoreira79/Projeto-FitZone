package com.fitzone.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;
import java.time.LocalDate;

@Entity
@Table(name = "alunos")
public class Aluno {
    
    /**
     * CPF deixou de ser PK. O id é um chave substituta(AUTO_INCREMENT),
     * e o CPF virou um campo UNIQUE comum 
     * - assim ele pode ser corrigido sem mexer em FKS e não aparece nas URLS da API
     */
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true,length = 11)
    private String cpf;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 20)
    private String telefone;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDate dataCadastro;

    protected Aluno() {}

    public Aluno(String cpf, String nome, String email, String telefone, LocalDate dataCadastro){
        this.cpf = cpf;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.dataCadastro = dataCadastro;
    }

    /** Getters e Setters */
    public Integer getId()             { return id; }
    public void setId(Integer id)      { this.id = id; }

    public String getCpf()             { return cpf; }
    public void setCpf(String cpf)     { this.cpf = cpf; }
 
    public String getNome()            { return nome; }
    public void setNome(String nome)   { this.nome = nome; }
 
    public String getEmail()           { return email; }
    public void setEmail(String email) { this.email = email; }
 
    public String getTelefone()                  { return telefone; }
    public void setTelefone(String telefone)     { this.telefone = telefone; }
 
    public LocalDate getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDate dataCadastro) { this.dataCadastro = dataCadastro; }

    @Override 
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Aluno outro)) return false;
        return id != null && Objects.equals(id, outro.id);
    }

    @Override 
    public int hashCode() { return getClass().hashCode(); }

    @Override
    public String toString(){
        return "Aluno [ID: " + getId() + ", CPF: " + cpf + ", Nome: " + nome + ", Email: " + email + ", Telefone: " + telefone + ", Cadastro: " + dataCadastro + "]";
    }
}
