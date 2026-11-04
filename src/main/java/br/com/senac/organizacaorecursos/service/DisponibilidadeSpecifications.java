package br.com.senac.organizacaorecursos.service;

import br.com.senac.organizacaorecursos.entity.Reserva;
import java.time.OffsetDateTime;
import org.springframework.data.jpa.domain.Specification;

final class DisponibilidadeSpecifications {
    private DisponibilidadeSpecifications() { }

    static <T> Specification<T> disponivel(String atributoRecurso, OffsetDateTime inicio, OffsetDateTime termino) {
        return (root, query, criteriaBuilder) -> {
            var reservas = query.subquery(Long.class);
            var reserva = reservas.from(Reserva.class);
            reservas.select(reserva.get("id"));
            reservas.where(
                    criteriaBuilder.equal(reserva.get(atributoRecurso).get("id"), root.get("id")),
                    reserva.get("status").in(ReservaService.STATUS_QUE_BLOQUEIAM_HORARIO),
                    criteriaBuilder.lessThan(reserva.get("inicio"), termino),
                    criteriaBuilder.greaterThan(reserva.get("termino"), inicio));
            return criteriaBuilder.not(criteriaBuilder.exists(reservas));
        };
    }
}
