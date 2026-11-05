package br.com.senac.organizacaorecursos.service;

import br.com.senac.organizacaorecursos.dto.ManutencaoRequest;
import br.com.senac.organizacaorecursos.entity.Material;
import br.com.senac.organizacaorecursos.entity.Manutencao;
import br.com.senac.organizacaorecursos.entity.Perfil;
import br.com.senac.organizacaorecursos.entity.Professor;
import br.com.senac.organizacaorecursos.entity.Sala;
import br.com.senac.organizacaorecursos.entity.StatusManutencao;
import br.com.senac.organizacaorecursos.entity.TipoRecurso;
import br.com.senac.organizacaorecursos.entity.Usuario;
import br.com.senac.organizacaorecursos.repository.ManutencaoRepository;
import br.com.senac.organizacaorecursos.repository.MaterialRepository;
import br.com.senac.organizacaorecursos.repository.ProfessorRepository;
import br.com.senac.organizacaorecursos.repository.ReservaRepository;
import br.com.senac.organizacaorecursos.repository.SalaRepository;
import br.com.senac.organizacaorecursos.repository.UsuarioRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("!test")
public class ManutencaoService {
    private final ManutencaoRepository manutencaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final SalaRepository salaRepository;
    private final ProfessorRepository professorRepository;
    private final MaterialRepository materialRepository;
    private final ReservaRepository reservaRepository;
    private final AuditoriaService auditoriaService;
    private final NotificacaoService notificacaoService;

    public ManutencaoService(ManutencaoRepository manutencaoRepository, UsuarioRepository usuarioRepository,
                             SalaRepository salaRepository, ProfessorRepository professorRepository,
                             MaterialRepository materialRepository, ReservaRepository reservaRepository,
                             AuditoriaService auditoriaService, NotificacaoService notificacaoService) {
        this.manutencaoRepository = manutencaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.salaRepository = salaRepository;
        this.professorRepository = professorRepository;
        this.materialRepository = materialRepository;
        this.reservaRepository = reservaRepository;
        this.auditoriaService = auditoriaService;
        this.notificacaoService = notificacaoService;
    }

    @Transactional
    public Manutencao criar(String emailAutenticado, ManutencaoRequest request) {
        Usuario responsavel = usuarioAtivo(emailAutenticado);
        if (responsavel.getPerfil() == Perfil.SOLICITANTE) {
            throw new ManutencaoOwnershipException();
        }

        Recurso recurso = resolverRecurso(request);
        bloquearRecurso(recurso);
        verificarConflito(recurso, request.inicio(), request.termino(), null);

        Manutencao manutencao = manutencaoRepository.save(new Manutencao(
                tipoDoRecurso(recurso),
                recurso.sala(),
                recurso.professor(),
                recurso.material(),
                request.inicio(),
                request.termino(),
                request.motivo(),
                responsavel,
                OffsetDateTime.now(),
                StatusManutencao.ATIVA));
        auditoriaService.registrar(emailAutenticado, "MANUTENCAO", manutencao.getId(), "CRIADA",
                null, manutencao.getStatus().name(), "Manutencao criada");
        notificacaoService.criarParaUsuario(responsavel.getEmail(), "MANUTENCAO_CRIADA",
                "A manutencao foi registrada no sistema.", "MANUTENCAO", manutencao.getId());
        return manutencao;
    }

    @Transactional(readOnly = true)
    public List<Manutencao> listar(String emailAutenticado) {
        Usuario usuario = usuarioAtivo(emailAutenticado);
        if (usuario.getPerfil() == Perfil.SOLICITANTE) {
            throw new ManutencaoOwnershipException();
        }
        if (usuario.getPerfil() == Perfil.ADMIN) {
            return manutencaoRepository.findAll();
        }
        return manutencaoRepository.findAllByResponsavel_Id(usuario.getId());
    }

    @Transactional(readOnly = true)
    public Manutencao buscarPorId(Long id, String emailAutenticado) {
        Usuario usuario = usuarioAtivo(emailAutenticado);
        if (usuario.getPerfil() == Perfil.SOLICITANTE) {
            throw new ManutencaoOwnershipException();
        }
        Manutencao manutencao = manutencaoRepository.findById(id).orElseThrow(ManutencaoNotFoundException::new);
        if (usuario.getPerfil() == Perfil.RESPONSAVEL && !manutencao.getResponsavel().getId().equals(usuario.getId())) {
            throw new ManutencaoOwnershipException();
        }
        return manutencao;
    }

    @Transactional
    public Manutencao cancelar(Long id, String emailAutenticado) {
        Usuario usuario = usuarioAtivo(emailAutenticado);
        if (usuario.getPerfil() == Perfil.SOLICITANTE) {
            throw new ManutencaoOwnershipException();
        }
        Manutencao manutencao = manutencaoRepository.findByIdParaAtualizacao(id).orElseThrow(ManutencaoNotFoundException::new);
        if (usuario.getPerfil() == Perfil.RESPONSAVEL && !manutencao.getResponsavel().getId().equals(usuario.getId())) {
            throw new ManutencaoOwnershipException();
        }
        StatusManutencao anterior = manutencao.getStatus();
        manutencao.cancelar();
        Manutencao salva = manutencaoRepository.save(manutencao);
        auditoriaService.registrar(emailAutenticado, "MANUTENCAO", salva.getId(), "CANCELADA",
                anterior.name(), salva.getStatus().name(), "Manutencao cancelada");
        notificacaoService.criarParaUsuario(salva.getResponsavel().getEmail(), "MANUTENCAO_CANCELADA",
                "A manutencao foi cancelada.", "MANUTENCAO", salva.getId());
        return salva;
    }

    private Usuario usuarioAtivo(String email) {
        return usuarioRepository.findByEmail(email).filter(Usuario::getAtivo)
                .orElseThrow(ManutencaoOwnershipException::new);
    }

    private TipoRecurso tipoDoRecurso(Recurso recurso) {
        if (recurso.sala() != null) return TipoRecurso.SALA;
        if (recurso.professor() != null) return TipoRecurso.PROFESSOR;
        return TipoRecurso.MATERIAL;
    }

    private void bloquearRecurso(Recurso recurso) {
        if (recurso.sala() != null) {
            salaRepository.findByIdParaReserva(recurso.sala().getId()).orElseThrow(SalaNotFoundException::new);
        }
        if (recurso.professor() != null) {
            professorRepository.findByIdParaReserva(recurso.professor().getId()).orElseThrow(ProfessorNotFoundException::new);
        }
        if (recurso.material() != null) {
            materialRepository.findByIdParaReserva(recurso.material().getId()).orElseThrow(MaterialNotFoundException::new);
        }
    }

    private Recurso resolverRecurso(ManutencaoRequest request) {
        Sala sala = request.salaId() == null ? null
                : salaRepository.findByIdParaReserva(request.salaId()).orElseThrow(SalaNotFoundException::new);
        Professor professor = request.professorId() == null ? null
                : professorRepository.findByIdParaReserva(request.professorId()).orElseThrow(ProfessorNotFoundException::new);
        Material material = request.materialId() == null ? null
                : materialRepository.findByIdParaReserva(request.materialId()).orElseThrow(MaterialNotFoundException::new);
        return new Recurso(sala, professor, material);
    }

    private void verificarConflito(Recurso recurso, OffsetDateTime inicio, OffsetDateTime termino, Long idExcluido) {
        Long salaId = recurso.sala() == null ? null : recurso.sala().getId();
        Long professorId = recurso.professor() == null ? null : recurso.professor().getId();
        Long materialId = recurso.material() == null ? null : recurso.material().getId();

        boolean conflitoReserva = reservaRepository.existsConflito(salaId, professorId, materialId,
                inicio, termino, ReservaService.STATUS_QUE_BLOQUEIAM_HORARIO, idExcluido);
        boolean conflitoManutencao = manutencaoRepository.existsConflito(salaId, professorId, materialId,
                inicio, termino, StatusManutencao.ATIVA, idExcluido);

        if (conflitoReserva || conflitoManutencao) {
            throw new ManutencaoConflitoException();
        }
    }

    private record Recurso(Sala sala, Professor professor, Material material) { }
}
