package br.com.senac.organizacaorecursos.repository;

import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface RelatorioRepository extends Repository<br.com.senac.organizacaorecursos.entity.Reserva, Long> {
    @Query(value = """
            SELECT CASE
                     WHEN r.sala_id IS NOT NULL THEN 'SALA'
                     WHEN r.professor_id IS NOT NULL THEN 'PROFESSOR'
                     ELSE 'MATERIAL'
                   END AS tipoRecurso,
                   COUNT(r.id) AS quantidadeReservas,
                   COALESCE(SUM(EXTRACT(EPOCH FROM (LEAST(r.termino, :termino)
                                                  - GREATEST(r.inicio, :inicio))) / 3600), 0)
                       AS horasUtilizadas
            FROM reservas r
            WHERE r.status IN ('APROVADA', 'EM_USO', 'CONCLUIDA')
              AND r.inicio < :termino
              AND r.termino > :inicio
            GROUP BY CASE
                       WHEN r.sala_id IS NOT NULL THEN 'SALA'
                       WHEN r.professor_id IS NOT NULL THEN 'PROFESSOR'
                       ELSE 'MATERIAL'
                     END
            ORDER BY tipoRecurso
            """, nativeQuery = true)
    List<UtilizacaoPorTipoProjection> buscarUtilizacaoPorTipo(
            @Param("inicio") OffsetDateTime inicio, @Param("termino") OffsetDateTime termino);

    @Query(value = """
            SELECT u.id AS solicitanteId,
                   u.email AS solicitanteEmail,
                   COUNT(r.id) AS quantidadeReservas,
                   COALESCE(SUM(EXTRACT(EPOCH FROM (LEAST(r.termino, :termino)
                                                  - GREATEST(r.inicio, :inicio))) / 3600), 0)
                       AS horasReservadas
            FROM reservas r
            JOIN usuarios u ON u.id = r.solicitante_id
            WHERE r.status IN ('APROVADA', 'EM_USO', 'CONCLUIDA')
              AND r.inicio < :termino
              AND r.termino > :inicio
            GROUP BY u.id, u.email
            ORDER BY u.email
            """, nativeQuery = true)
    List<CargaHorariaPorSolicitanteProjection> buscarCargaHorariaPorSolicitante(
            @Param("inicio") OffsetDateTime inicio, @Param("termino") OffsetDateTime termino);
}
