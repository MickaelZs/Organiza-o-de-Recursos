package br.com.senac.organizacaorecursos.repository;

import br.com.senac.organizacaorecursos.entity.Professor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {
    boolean existsByEmail(String email);
}
