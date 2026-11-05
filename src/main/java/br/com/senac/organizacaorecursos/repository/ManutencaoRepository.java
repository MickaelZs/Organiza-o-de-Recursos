package br.com.senac.organizacaorecursos.repository;

import br.com.senac.organizacaorecursos.entity.Manutencao;
import br.com.senac.organizacaorecursos.entity.StatusManutencao;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ManutencaoRepository extends JpaRepository<Manutencao, Long> {
    List<Manutencao> findAllByResponsavel_Id(Long responsavelId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select manutencao from Manutencao manutencao where manutencao.id = :id")
    Optional<Manutencao> findByIdParaAtualizacao(@Param("id") Long id);

    @Query("""
            select case when count(m) > 0 then true else false end
            from Manutencao m
            where m.status = :status
              and m.inicio < :termino
              and m.termino > :inicio
              and ((:salaId is not null and m.sala.id = :salaId)
                or (:professorId is not null and m.professor.id = :professorId)
                or (:materialId is not null and m.material.id = :materialId))
              and (:idExcluido is null or m.id <> :idExcluido)
            """)
    boolean existsConflito(@Param("salaId") Long salaId,
                           @Param("professorId") Long professorId,
                           @Param("materialId") Long materialId,
                           @Param("inicio") OffsetDateTime inicio,
                           @Param("termino") OffsetDateTime termino,
                           @Param("status") StatusManutencao status,
                           @Param("idExcluido") Long idExcluido);
}
