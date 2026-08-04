package entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "alunos")
public class Aluno implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @Column(length = 11)
    private String cpf;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
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
    public String toString(){
        return "Aluno [CPF: " + cpf + ", Nome: " + nome + ", Email: " + email + ", Telefone: " + telefone + ", Cadastro: " + dataCadastro + "]";
    }
}
