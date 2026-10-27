package br.com.senac.organizacaorecursos.repository;

import br.com.senac.organizacaorecursos.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialRepository extends JpaRepository<Material, Long> { }
