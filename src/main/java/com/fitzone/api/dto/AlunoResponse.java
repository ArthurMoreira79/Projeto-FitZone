package com.fitzone.api.dto;

import com.fitzone.domain.model.Aluno;

import java.time.LocalDate;

public record AlunoResponse(
        Integer id,
        String cpf,
        String nome,
        String email,
        String telefone,
        LocalDate dataCadastro
) {
    public static AlunoResponse from(Aluno aluno) {
        return new AlunoResponse(aluno.getId(), aluno.getCpf(), aluno.getNome(),
                aluno.getEmail(), aluno.getTelefone(), aluno.getDataCadastro());
    }
}