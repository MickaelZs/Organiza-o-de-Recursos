package br.com.senac.organizacaorecursos.repository;

import br.com.senac.organizacaorecursos.entity.Reserva;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findAllBySolicitante_Id(Long solicitanteId);
}
