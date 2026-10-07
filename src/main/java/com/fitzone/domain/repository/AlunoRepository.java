package com.fitzone.domain.repository;

import com.fitzone.domain.model.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlunoRepository extends JpaRepository<Aluno, Integer>{
    
    /*O CPF deixou de ser a PK, então a busca por CPF virou um query method */
    Optional<Aluno> findByCpf(String cpf);

    /*Unicidade no cadastro (POST): CPF e email são UNIQUE no banco */
    boolean existsByCpf(String cpf);
    boolean existsByEmail(String email); 

    /**
     * Unicidade na atualização (PUT):
     * ignora o próprio aluno, senão salvar sem mudar o CPF acusaria "CPF já cadastrado"
     */
    boolean existsByCpfAndIdNot(String cpf, Integer id);
    boolean existsByEmailAndIdNot(String email, Integer id);
}
