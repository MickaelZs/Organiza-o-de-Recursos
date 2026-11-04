package br.com.senac.organizacaorecursos.repository;

import br.com.senac.organizacaorecursos.entity.Material;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MaterialRepository extends JpaRepository<Material, Long>, JpaSpecificationExecutor<Material> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select material from Material material where material.id = :id")
    Optional<Material> findByIdParaReserva(@Param("id") Long id);
}
