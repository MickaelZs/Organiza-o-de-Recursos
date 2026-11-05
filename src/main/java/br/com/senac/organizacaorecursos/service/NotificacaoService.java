package br.com.senac.organizacaorecursos.service;

import br.com.senac.organizacaorecursos.entity.Notificacao;
import br.com.senac.organizacaorecursos.entity.Perfil;
import br.com.senac.organizacaorecursos.entity.Usuario;
import br.com.senac.organizacaorecursos.repository.NotificacaoRepository;
import br.com.senac.organizacaorecursos.repository.UsuarioRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificacaoService {
    private final NotificacaoRepository notificacaoRepository;
    private final UsuarioRepository usuarioRepository;

    public NotificacaoService(NotificacaoRepository notificacaoRepository, UsuarioRepository usuarioRepository) {
        this.notificacaoRepository = notificacaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Notificacao criarParaUsuario(String emailDestinatario, String tipo, String mensagem,
                                       String entidadeTipo, Long entidadeId) {
        Usuario destinatario = usuarioRepository.findByEmail(emailDestinatario)
                .filter(Usuario::getAtivo)
                .orElseThrow(ReservaOwnershipException::new);
        return notificacaoRepository.save(new Notificacao(destinatario, tipo, mensagem,
                OffsetDateTime.now(), false, entidadeTipo, entidadeId));
    }

    @Transactional(readOnly = true)
    public List<Notificacao> listar(String emailAutenticado) {
        Usuario usuario = usuarioRepository.findByEmail(emailAutenticado)
                .filter(Usuario::getAtivo)
                .orElseThrow(ReservaOwnershipException::new);
        return notificacaoRepository.findAllByDestinatario_IdOrderByCriadaEmDesc(usuario.getId());
    }

    @Transactional
    public Notificacao marcarComoLida(Long id, String emailAutenticado) {
        Usuario usuario = usuarioRepository.findByEmail(emailAutenticado)
                .filter(Usuario::getAtivo)
                .orElseThrow(ReservaOwnershipException::new);
        Notificacao notificacao = notificacaoRepository.findById(id)
                .orElseThrow(ReservaNotFoundException::new);
        if (!notificacao.getDestinatario().getId().equals(usuario.getId()) && usuario.getPerfil() != Perfil.ADMIN) {
            throw new ReservaOwnershipException();
        }
        notificacao.marcarComoLida(OffsetDateTime.now());
        return notificacaoRepository.save(notificacao);
    }
}
