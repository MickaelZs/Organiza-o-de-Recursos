package br.com.senac.organizacaorecursos.repository;

import br.com.senac.organizacaorecursos.entity.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProfessorRepository extends JpaRepository<Professor, Long>, JpaSpecificationExecutor<Professor> {
    boolean existsByEmail(String email);
}
