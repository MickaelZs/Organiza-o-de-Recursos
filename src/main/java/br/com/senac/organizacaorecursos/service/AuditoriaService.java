package br.com.senac.organizacaorecursos.service;

import br.com.senac.organizacaorecursos.entity.Auditoria;
import br.com.senac.organizacaorecursos.entity.Perfil;
import br.com.senac.organizacaorecursos.entity.Usuario;
import br.com.senac.organizacaorecursos.repository.AuditoriaRepository;
import br.com.senac.organizacaorecursos.repository.UsuarioRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

@Service
public class AuditoriaService {
    private final AuditoriaRepository auditoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper;

    public AuditoriaService(AuditoriaRepository auditoriaRepository, UsuarioRepository usuarioRepository,
                            ObjectMapper objectMapper) {
        this.auditoriaRepository = auditoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.objectMapper = objectMapper;
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

    @Transactional
    public Auditoria registrar(String emailAutenticado, String tipoEntidade, Long entidadeId,
                               String acao, Object estadoAnterior, Object estadoPosterior,
                               Map<String, Object> detalhes) {
        return registrar(emailAutenticado, tipoEntidade, entidadeId, acao, estadoAnterior,
                estadoPosterior, serializarDetalhes(detalhes));
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Auditoria registrarConflitoEvitado(String emailAutenticado, String tipoRecurso, Long recursoId,
                                              Map<String, Object> detalhes) {
        return registrarConflitoEvitado(emailAutenticado, tipoRecurso, recursoId, serializarDetalhes(detalhes));
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

    private String serializarDetalhes(Map<String, Object> detalhes) {
        try {
            return objectMapper.writeValueAsString(detalhes);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Nao foi possivel serializar os detalhes da auditoria.", exception);
        }
    }
}
