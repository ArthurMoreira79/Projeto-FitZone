package dto;

import entidades.Aluno;

import java.time.LocalDate;

public record AlunoResponse(
        String cpf,
        String nome,
        String email,
        String telefone,
        LocalDate dataCadastro
) {
    public static AlunoResponse from(Aluno a) {
        return new AlunoResponse(a.getCpf(), a.getNome(), a.getEmail(), a.getTelefone(), a.getDataCadastro());
    }
}
