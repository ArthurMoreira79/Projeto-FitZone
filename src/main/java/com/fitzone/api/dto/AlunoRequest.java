package com.fitzone.api.dto;

import com.fitzone.domain.model.Aluno;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Usado no POST e no PUT. Sem id e sem dataCadastro: são definidos pelo sistema.
public record AlunoRequest(

        @NotBlank(message = "O CPF é obrigatório")
        @Pattern(regexp = "\\d{11}", message = "O CPF deve conter exatamente 11 dígitos, sem pontos ou traço")
        String cpf,

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 100, message = "O e-mail deve ter no máximo 100 caracteres")
        String email,

        @NotBlank(message = "O telefone é obrigatório")
        @Pattern(regexp = "\\d{10,11}", message = "O telefone deve conter DDD + número (10 ou 11 dígitos), sem máscara")
        String telefone
) {
    // dataCadastro vai null: quem define é o AlunoService.cadastrar()
    public Aluno toEntity() {
        return new Aluno(cpf, nome, email, telefone, null);
    }
}