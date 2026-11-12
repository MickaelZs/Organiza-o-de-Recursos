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
            SELECT CASE
                     WHEN r.sala_id IS NOT NULL THEN 'SALA'
                     WHEN r.professor_id IS NOT NULL THEN 'PROFESSOR'
                     ELSE 'MATERIAL'
                   END AS tipoRecurso,
                   CASE
                     WHEN r.sala_id IS NOT NULL THEN r.sala_id
                     WHEN r.professor_id IS NOT NULL THEN r.professor_id
                     ELSE r.material_id
                   END AS recursoId,
                   CASE
                     WHEN r.sala_id IS NOT NULL THEN s.nome
                     WHEN r.professor_id IS NOT NULL THEN p.nome
                     ELSE m.nome
                   END AS nomeRecurso,
                   CASE
                     WHEN r.sala_id IS NOT NULL THEN s.tipo
                     WHEN r.professor_id IS NOT NULL THEN p.competencia
                     ELSE m.tipo
                   END AS categoria,
                   COUNT(r.id) AS quantidadeReservas,
                   COALESCE(SUM(EXTRACT(EPOCH FROM (LEAST(r.termino, :termino)
                                                  - GREATEST(r.inicio, :inicio))) / 3600), 0)
                       AS horasUtilizadas
            FROM reservas r
            LEFT JOIN salas s ON s.id = r.sala_id
            LEFT JOIN professores p ON p.id = r.professor_id
            LEFT JOIN materiais m ON m.id = r.material_id
            WHERE r.status IN ('APROVADA', 'EM_USO', 'CONCLUIDA')
              AND r.inicio < :termino
              AND r.termino > :inicio
            GROUP BY CASE
                       WHEN r.sala_id IS NOT NULL THEN 'SALA'
                       WHEN r.professor_id IS NOT NULL THEN 'PROFESSOR'
                       ELSE 'MATERIAL'
                     END,
                     CASE
                       WHEN r.sala_id IS NOT NULL THEN r.sala_id
                       WHEN r.professor_id IS NOT NULL THEN r.professor_id
                       ELSE r.material_id
                     END,
                     CASE
                       WHEN r.sala_id IS NOT NULL THEN s.nome
                       WHEN r.professor_id IS NOT NULL THEN p.nome
                       ELSE m.nome
                     END,
                     CASE
                       WHEN r.sala_id IS NOT NULL THEN s.tipo
                       WHEN r.professor_id IS NOT NULL THEN p.competencia
                       ELSE m.tipo
                     END
            ORDER BY tipoRecurso, recursoId
            """, nativeQuery = true)
    List<UtilizacaoPorRecursoProjection> buscarUtilizacaoPorRecurso(
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

    @Query(value = """
            SELECT p.id AS professorId,
                   p.nome AS professorNome,
                   p.competencia AS competencia,
                   COUNT(r.id) AS quantidadeReservas,
                   COALESCE(SUM(EXTRACT(EPOCH FROM (LEAST(r.termino, :termino)
                                                  - GREATEST(r.inicio, :inicio))) / 3600), 0)
                       AS horasAlocadas
            FROM reservas r
            JOIN professores p ON p.id = r.professor_id
            WHERE r.status IN ('APROVADA', 'EM_USO', 'CONCLUIDA')
              AND r.inicio < :termino
              AND r.termino > :inicio
            GROUP BY p.id, p.nome, p.competencia
            ORDER BY p.nome, p.id
            """, nativeQuery = true)
    List<CargaHorariaPorProfessorProjection> buscarCargaHorariaPorProfessor(
            @Param("inicio") OffsetDateTime inicio, @Param("termino") OffsetDateTime termino);
}
