package br.com.senac.organizacaorecursos.dto;

import br.com.senac.organizacaorecursos.entity.Manutencao;
import br.com.senac.organizacaorecursos.entity.StatusManutencao;
import br.com.senac.organizacaorecursos.entity.TipoRecurso;
import java.time.OffsetDateTime;

public record ManutencaoResponse(
        Long id,
        TipoRecurso tipoRecurso,
        Long salaId,
        Long professorId,
        Long materialId,
        OffsetDateTime inicio,
        OffsetDateTime termino,
        String motivo,
        String responsavelEmail,
        OffsetDateTime criadoEm,
        StatusManutencao status) {

    public static ManutencaoResponse from(Manutencao manutencao) {
        return new ManutencaoResponse(
                manutencao.getId(),
                manutencao.getTipoRecurso(),
                manutencao.getSala() == null ? null : manutencao.getSala().getId(),
                manutencao.getProfessor() == null ? null : manutencao.getProfessor().getId(),
                manutencao.getMaterial() == null ? null : manutencao.getMaterial().getId(),
                manutencao.getInicio(),
                manutencao.getTermino(),
                manutencao.getMotivo(),
                manutencao.getResponsavel() == null ? null : manutencao.getResponsavel().getEmail(),
                manutencao.getCriadoEm(),
                manutencao.getStatus());
    }
}
