package br.com.senac.organizacaorecursos.service;

import br.com.senac.organizacaorecursos.entity.Manutencao;
import br.com.senac.organizacaorecursos.entity.Reserva;
import br.com.senac.organizacaorecursos.entity.StatusManutencao;
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

            var manutencoes = query.subquery(Long.class);
            var manutencao = manutencoes.from(Manutencao.class);
            manutencoes.select(manutencao.get("id"));
            manutencoes.where(
                    criteriaBuilder.equal(manutencao.get(atributoRecurso).get("id"), root.get("id")),
                    manutencao.get("status").in(StatusManutencao.ATIVA),
                    criteriaBuilder.lessThan(manutencao.get("inicio"), termino),
                    criteriaBuilder.greaterThan(manutencao.get("termino"), inicio));

            var indisponivel = criteriaBuilder.or(criteriaBuilder.exists(reservas), criteriaBuilder.exists(manutencoes));
            return criteriaBuilder.not(indisponivel);
        };
    }
}
