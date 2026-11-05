package br.com.senac.organizacaorecursos.repository;

import br.com.senac.organizacaorecursos.entity.Auditoria;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
    @EntityGraph(attributePaths = "usuario")
    List<Auditoria> findAllByOrderByCriadoEmDesc();
}
