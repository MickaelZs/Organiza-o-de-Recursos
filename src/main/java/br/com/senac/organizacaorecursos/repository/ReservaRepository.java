package br.com.senac.organizacaorecursos.repository;

import br.com.senac.organizacaorecursos.entity.Reserva;
import br.com.senac.organizacaorecursos.entity.StatusReserva;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findAllBySolicitante_Id(Long solicitanteId);

    @Query("""
            select case when count(r) > 0 then true else false end
            from Reserva r
            where r.status in :statuses
              and r.inicio < :termino
              and r.termino > :inicio
              and ((:salaId is not null and r.sala.id = :salaId)
                or (:professorId is not null and r.professor.id = :professorId)
                or (:materialId is not null and r.material.id = :materialId))
              and (:idExcluido is null or r.id <> :idExcluido)
            """)
    boolean existsConflito(@Param("salaId") Long salaId,
                           @Param("professorId") Long professorId,
                           @Param("materialId") Long materialId,
                           @Param("inicio") OffsetDateTime inicio,
                           @Param("termino") OffsetDateTime termino,
                           @Param("statuses") Collection<StatusReserva> statuses,
                           @Param("idExcluido") Long idExcluido);
}
