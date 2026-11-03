package br.com.senac.organizacaorecursos.repository;

import br.com.senac.organizacaorecursos.entity.Sala;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SalaRepository extends JpaRepository<Sala, Long>, JpaSpecificationExecutor<Sala> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select sala from Sala sala where sala.id = :id")
    Optional<Sala> findByIdParaReserva(@Param("id") Long id);
}
