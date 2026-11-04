package br.com.senac.organizacaorecursos.service;

import br.com.senac.organizacaorecursos.dto.ReservaRequest;
import br.com.senac.organizacaorecursos.entity.Material;
import br.com.senac.organizacaorecursos.entity.Perfil;
import br.com.senac.organizacaorecursos.entity.Professor;
import br.com.senac.organizacaorecursos.entity.Reserva;
import br.com.senac.organizacaorecursos.entity.Sala;
import br.com.senac.organizacaorecursos.entity.StatusReserva;
import br.com.senac.organizacaorecursos.entity.Usuario;
import br.com.senac.organizacaorecursos.repository.MaterialRepository;
import br.com.senac.organizacaorecursos.repository.ProfessorRepository;
import br.com.senac.organizacaorecursos.repository.ReservaRepository;
import br.com.senac.organizacaorecursos.repository.SalaRepository;
import br.com.senac.organizacaorecursos.repository.UsuarioRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservaService {
    static final List<StatusReserva> STATUS_QUE_BLOQUEIAM_HORARIO = List.copyOf(EnumSet.of(
            StatusReserva.SOLICITADA, StatusReserva.APROVADA, StatusReserva.EM_USO, StatusReserva.CONCLUIDA));

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final SalaRepository salaRepository;
    private final ProfessorRepository professorRepository;
    private final MaterialRepository materialRepository;

    public ReservaService(ReservaRepository reservaRepository, UsuarioRepository usuarioRepository,
                          SalaRepository salaRepository, ProfessorRepository professorRepository,
                          MaterialRepository materialRepository) {
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.salaRepository = salaRepository;
        this.professorRepository = professorRepository;
        this.materialRepository = materialRepository;
    }

    @Transactional
    public Reserva criar(String emailAutenticado, ReservaRequest request) {
        Usuario solicitante = usuarioAtivo(emailAutenticado);
        if (solicitante.getPerfil() != Perfil.SOLICITANTE) {
            throw new ReservaOwnershipException();
        }
        var recurso = resolverRecurso(request);
        verificarConflito(recurso, request, null);
        return reservaRepository.save(new Reserva(solicitante, recurso.sala(), recurso.professor(), recurso.material(),
                request.inicio(), request.termino(), statusInicial(recurso)));
    }

    @Transactional(readOnly = true)
    public List<Reserva> listar(String emailAutenticado) {
        Usuario usuario = usuarioAtivo(emailAutenticado);
        if (usuario.getPerfil() == Perfil.SOLICITANTE) {
            return reservaRepository.findAllBySolicitante_Id(usuario.getId());
        }
        return reservaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Reserva buscarPorId(Long id, String emailAutenticado) {
        Usuario usuario = usuarioAtivo(emailAutenticado);
        Reserva reserva = reservaRepository.findById(id).orElseThrow(ReservaNotFoundException::new);
        verificarConsultaPermitida(reserva, usuario);
        return reserva;
    }

    @Transactional
    public Reserva atualizar(Long id, String emailAutenticado, ReservaRequest request) {
        Usuario usuario = usuarioSolicitante(emailAutenticado);
        Reserva reserva = reservaRepository.findByIdParaAtualizacao(id).orElseThrow(ReservaNotFoundException::new);
        verificarProprietario(reserva, usuario);
        Recurso recurso = bloquearRecursosParaAtualizacao(reserva, request);
        if (STATUS_QUE_BLOQUEIAM_HORARIO.contains(reserva.getStatus())) {
            verificarConflito(recurso, request, reserva.getId());
        }
        StatusReserva status = reserva.getStatus() == StatusReserva.SOLICITADA
                || reserva.getStatus() == StatusReserva.APROVADA ? statusInicial(recurso) : reserva.getStatus();
        reserva.atualizar(recurso.sala(), recurso.professor(), recurso.material(), request.inicio(), request.termino(), status);
        return reservaRepository.save(reserva);
    }

    @Transactional
    public Reserva aprovar(Long id, String emailAutenticado) {
        usuarioResponsavel(emailAutenticado);
        Reserva reserva = reservaRepository.findByIdParaAtualizacao(id).orElseThrow(ReservaNotFoundException::new);
        validarSolicitacaoRestritaPendente(reserva);
        Recurso recurso = bloquearRecursoDaReserva(reserva);
        verificarConflito(recurso, reserva.getInicio(), reserva.getTermino(), reserva.getId());
        reserva.aprovar();
        return reservaRepository.save(reserva);
    }

    @Transactional
    public Reserva rejeitar(Long id, String emailAutenticado) {
        usuarioResponsavel(emailAutenticado);
        Reserva reserva = reservaRepository.findByIdParaAtualizacao(id).orElseThrow(ReservaNotFoundException::new);
        validarSolicitacaoRestritaPendente(reserva);
        reserva.rejeitar();
        return reservaRepository.save(reserva);
    }

    @Transactional
    public void cancelar(Long id, String emailAutenticado) {
        Usuario usuario = usuarioSolicitante(emailAutenticado);
        Reserva reserva = reservaRepository.findById(id).orElseThrow(ReservaNotFoundException::new);
        verificarProprietario(reserva, usuario);
        reserva.cancelar();
        reservaRepository.save(reserva);
    }

    private Usuario usuarioAtivo(String email) {
        return usuarioRepository.findByEmail(email).filter(Usuario::getAtivo)
                .orElseThrow(ReservaOwnershipException::new);
    }

    private Usuario usuarioSolicitante(String email) {
        Usuario usuario = usuarioAtivo(email);
        if (usuario.getPerfil() != Perfil.SOLICITANTE) throw new ReservaOwnershipException();
        return usuario;
    }

    private void usuarioResponsavel(String email) {
        Usuario usuario = usuarioAtivo(email);
        if (usuario.getPerfil() != Perfil.RESPONSAVEL) throw new ReservaAprovacaoNaoPermitidaException();
    }

    private void validarSolicitacaoRestritaPendente(Reserva reserva) {
        if (reserva.getStatus() != StatusReserva.SOLICITADA || !reserva.envolveRecursoRestrito()) {
            throw new ReservaEstadoInvalidoException();
        }
    }

    private StatusReserva statusInicial(Recurso recurso) {
        boolean restrito = recurso.sala() != null && Boolean.TRUE.equals(recurso.sala().getRestrita())
                || recurso.material() != null && Boolean.TRUE.equals(recurso.material().getRestrito());
        return restrito ? StatusReserva.SOLICITADA : StatusReserva.APROVADA;
    }

    private Recurso bloquearRecursoDaReserva(Reserva reserva) {
        if (reserva.getSala() != null) {
            Sala sala = salaRepository.findByIdParaReserva(reserva.getSala().getId())
                    .orElseThrow(SalaNotFoundException::new);
            return new Recurso(sala, null, null);
        }
        if (reserva.getProfessor() != null) {
            Professor professor = professorRepository.findByIdParaReserva(reserva.getProfessor().getId())
                    .orElseThrow(ProfessorNotFoundException::new);
            return new Recurso(null, professor, null);
        }
        Material material = materialRepository.findByIdParaReserva(reserva.getMaterial().getId())
                .orElseThrow(MaterialNotFoundException::new);
        return new Recurso(null, null, material);
    }

    private void verificarConsultaPermitida(Reserva reserva, Usuario usuario) {
        if (usuario.getPerfil() == Perfil.SOLICITANTE) verificarProprietario(reserva, usuario);
    }

    private void verificarProprietario(Reserva reserva, Usuario usuario) {
        if (!reserva.getSolicitante().getId().equals(usuario.getId())) throw new ReservaOwnershipException();
    }

    private Recurso resolverRecurso(ReservaRequest request) {
        Sala sala = request.salaId() == null ? null
                : salaRepository.findByIdParaReserva(request.salaId()).orElseThrow(SalaNotFoundException::new);
        Professor professor = request.professorId() == null ? null
                : professorRepository.findByIdParaReserva(request.professorId()).orElseThrow(ProfessorNotFoundException::new);
        Material material = request.materialId() == null ? null
                : materialRepository.findByIdParaReserva(request.materialId()).orElseThrow(MaterialNotFoundException::new);
        return new Recurso(sala, professor, material);
    }

    private Recurso bloquearRecursosParaAtualizacao(Reserva reserva, ReservaRequest request) {
        List<RecursoLock> bloqueios = new ArrayList<>();
        adicionarBloqueio(bloqueios, TipoRecurso.SALA, reserva.getSala() == null ? null : reserva.getSala().getId());
        adicionarBloqueio(bloqueios, TipoRecurso.PROFESSOR, reserva.getProfessor() == null ? null : reserva.getProfessor().getId());
        adicionarBloqueio(bloqueios, TipoRecurso.MATERIAL, reserva.getMaterial() == null ? null : reserva.getMaterial().getId());
        adicionarBloqueio(bloqueios, TipoRecurso.SALA, request.salaId());
        adicionarBloqueio(bloqueios, TipoRecurso.PROFESSOR, request.professorId());
        adicionarBloqueio(bloqueios, TipoRecurso.MATERIAL, request.materialId());

        Map<String, RecursoLock> bloqueiosUnicos = new LinkedHashMap<>();
        for (RecursoLock bloqueio : bloqueios) {
            if (bloqueio.id() != null) {
                bloqueiosUnicos.putIfAbsent(bloqueio.tipo().name() + ':' + bloqueio.id(), bloqueio);
            }
        }

        List<RecursoLock> bloqueiosOrdenados = new ArrayList<>(bloqueiosUnicos.values());
        bloqueiosOrdenados.sort(Comparator.comparingInt(RecursoLock::ordem).thenComparing(RecursoLock::id));

        for (RecursoLock bloqueio : bloqueiosOrdenados) {
            switch (bloqueio.tipo()) {
                case SALA -> salaRepository.findByIdParaReserva(bloqueio.id())
                        .orElseThrow(SalaNotFoundException::new);
                case PROFESSOR -> professorRepository.findByIdParaReserva(bloqueio.id())
                        .orElseThrow(ProfessorNotFoundException::new);
                case MATERIAL -> materialRepository.findByIdParaReserva(bloqueio.id())
                        .orElseThrow(MaterialNotFoundException::new);
            }
        }

        return resolverRecurso(request);
    }

    private void adicionarBloqueio(List<RecursoLock> bloqueios, TipoRecurso tipo, Long id) {
        if (id != null) {
            bloqueios.add(new RecursoLock(tipo, id));
        }
    }

    private void verificarConflito(Recurso recurso, ReservaRequest request, Long idExcluido) {
        verificarConflito(recurso, request.inicio(), request.termino(), idExcluido);
    }

    private void verificarConflito(Recurso recurso, java.time.OffsetDateTime inicio,
                                   java.time.OffsetDateTime termino, Long idExcluido) {
        Long salaId = recurso.sala() == null ? null : recurso.sala().getId();
        Long professorId = recurso.professor() == null ? null : recurso.professor().getId();
        Long materialId = recurso.material() == null ? null : recurso.material().getId();
        if (reservaRepository.existsConflito(salaId, professorId, materialId,
                inicio, termino, STATUS_QUE_BLOQUEIAM_HORARIO, idExcluido)) {
            throw new ReservaConflitoException();
        }
    }

    private record Recurso(Sala sala, Professor professor, Material material) { }
    private record RecursoLock(TipoRecurso tipo, Long id) {
        int ordem() {
            return tipo.ordinal();
        }
    }
    private enum TipoRecurso {
        SALA,
        PROFESSOR,
        MATERIAL
    }
}
