package br.com.senac.organizacaorecursos.repository;

import br.com.senac.organizacaorecursos.entity.Sala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SalaRepository extends JpaRepository<Sala, Long>, JpaSpecificationExecutor<Sala> { }
