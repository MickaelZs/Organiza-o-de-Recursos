package br.com.senac.organizacaorecursos.service;

import br.com.senac.organizacaorecursos.entity.Auditoria;
import br.com.senac.organizacaorecursos.entity.Perfil;
import br.com.senac.organizacaorecursos.entity.Usuario;
import br.com.senac.organizacaorecursos.repository.AuditoriaRepository;
import br.com.senac.organizacaorecursos.repository.UsuarioRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

@Service
public class AuditoriaService {
    private final AuditoriaRepository auditoriaRepository;
    private final UsuarioRepository usuarioRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository, UsuarioRepository usuarioRepository) {
        this.auditoriaRepository = auditoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Auditoria registrar(String emailAutenticado, String tipoEntidade, Long entidadeId,
                               String acao, Object estadoAnterior, Object estadoPosterior, String detalhes) {
        Usuario usuario = usuarioRepository.findByEmail(emailAutenticado)
                .filter(Usuario::getAtivo)
                .orElseThrow(ReservaOwnershipException::new);
        return auditoriaRepository.save(new Auditoria(
                usuario,
                tipoEntidade,
                entidadeId,
                acao,
                estadoAnterior == null ? null : estadoAnterior.toString(),
                estadoPosterior == null ? null : estadoPosterior.toString(),
                detalhes,
                OffsetDateTime.now()));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Auditoria registrarConflitoEvitado(String emailAutenticado, String tipoRecurso, Long recursoId,
                                              String detalhes) {
        Usuario usuario = usuarioRepository.findByEmail(emailAutenticado)
                .filter(Usuario::getAtivo)
                .orElseThrow(ReservaOwnershipException::new);
        return auditoriaRepository.save(new Auditoria(
                usuario, "RECURSO", recursoId, "CONFLITO_EVITADO", null, null,
                "Conflito evitado para " + tipoRecurso + ": " + detalhes, OffsetDateTime.now()));
    }

    @Transactional(readOnly = true)
    public List<Auditoria> listar(String emailAutenticado) {
        Usuario usuario = usuarioRepository.findByEmail(emailAutenticado)
                .filter(Usuario::getAtivo)
                .orElseThrow(ReservaOwnershipException::new);
        if (usuario.getPerfil() == Perfil.SOLICITANTE) {
            throw new ReservaOwnershipException();
        }
        return auditoriaRepository.findAllByOrderByCriadoEmDesc();
    }
}
