package br.com.senac.organizacaorecursos.repository;

import br.com.senac.organizacaorecursos.entity.Auditoria;
import java.util.List;
import java.time.OffsetDateTime;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
    @EntityGraph(attributePaths = "usuario")
    List<Auditoria> findAllByOrderByCriadoEmDesc();

    @Query("""
            select count(a) from Auditoria a
            where a.acao = :acao
              and a.criadoEm >= :inicio
              and a.criadoEm < :termino
            """)
    long countByAcaoAndCriadoEmGreaterThanEqualAndCriadoEmLessThan(
            @Param("acao") String acao, @Param("inicio") OffsetDateTime inicio,
            @Param("termino") OffsetDateTime termino);
}
