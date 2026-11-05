package br.com.senac.organizacaorecursos.repository;

import br.com.senac.organizacaorecursos.entity.Notificacao;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
    @EntityGraph(attributePaths = "destinatario")
    List<Notificacao> findAllByDestinatario_IdOrderByCriadaEmDesc(Long destinatarioId);

    @EntityGraph(attributePaths = "destinatario")
    List<Notificacao> findAllByOrderByCriadaEmDesc();
}
