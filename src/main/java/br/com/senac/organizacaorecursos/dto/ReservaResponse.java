package br.com.senac.organizacaorecursos.dto;

import br.com.senac.organizacaorecursos.entity.Reserva;
import br.com.senac.organizacaorecursos.entity.StatusReserva;
import java.time.OffsetDateTime;

public record ReservaResponse(Long id, Long solicitanteId, Long salaId, Long professorId, Long materialId,
                              OffsetDateTime inicio, OffsetDateTime termino, StatusReserva status,
                              OffsetDateTime retiradoEm, OffsetDateTime devolvidoEm) {
    public static ReservaResponse from(Reserva reserva) {
        return new ReservaResponse(reserva.getId(), reserva.getSolicitante().getId(),
                reserva.getSala() == null ? null : reserva.getSala().getId(),
                reserva.getProfessor() == null ? null : reserva.getProfessor().getId(),
                reserva.getMaterial() == null ? null : reserva.getMaterial().getId(),
                reserva.getInicio(), reserva.getTermino(), reserva.getStatus(),
                reserva.getRetiradoEm(), reserva.getDevolvidoEm());
    }
}
