package dto;

import entidades.Aluno;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record AlunoRequest(
        @NotBlank(message = "CPF é obrigatório")
        @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 dígitos numéricos")
        String cpf,

        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @Email(message = "Email inválido")
        String email,

        String telefone
) {
    public Aluno toEntity() {
        return new Aluno(cpf, nome, email, telefone, LocalDate.now());
    }
}
