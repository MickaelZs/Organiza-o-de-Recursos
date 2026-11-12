package br.com.senac.organizacaorecursos.service;

import br.com.senac.organizacaorecursos.dto.ReservaRequest;
import br.com.senac.organizacaorecursos.entity.Material;
import br.com.senac.organizacaorecursos.entity.Perfil;
import br.com.senac.organizacaorecursos.entity.Professor;
import br.com.senac.organizacaorecursos.entity.Reserva;
import br.com.senac.organizacaorecursos.entity.Sala;
import br.com.senac.organizacaorecursos.entity.StatusManutencao;
import br.com.senac.organizacaorecursos.entity.StatusReserva;
import br.com.senac.organizacaorecursos.entity.Usuario;
import br.com.senac.organizacaorecursos.repository.ManutencaoRepository;
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
    private final ManutencaoRepository manutencaoRepository;
    private final AuditoriaService auditoriaService;
    private final NotificacaoService notificacaoService;

    public ReservaService(ReservaRepository reservaRepository, UsuarioRepository usuarioRepository,
                          SalaRepository salaRepository, ProfessorRepository professorRepository,
                          MaterialRepository materialRepository, ManutencaoRepository manutencaoRepository,
                          AuditoriaService auditoriaService, NotificacaoService notificacaoService) {
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.salaRepository = salaRepository;
        this.professorRepository = professorRepository;
        this.materialRepository = materialRepository;
        this.manutencaoRepository = manutencaoRepository;
        this.auditoriaService = auditoriaService;
        this.notificacaoService = notificacaoService;
    }

    @Transactional(noRollbackFor = ReservaConflitoException.class)
    public Reserva criar(String emailAutenticado, ReservaRequest request) {
        Usuario solicitante = usuarioAtivo(emailAutenticado);
        if (solicitante.getPerfil() != Perfil.SOLICITANTE) {
            throw new ReservaOwnershipException();
        }
        var recurso = resolverRecurso(request);
        verificarConflito(emailAutenticado, recurso, request, null);
        Reserva reserva = reservaRepository.save(new Reserva(solicitante, recurso.sala(), recurso.professor(), recurso.material(),
                request.inicio(), request.termino(), statusInicial(recurso)));
        auditoriaService.registrar(emailAutenticado, "RESERVA", reserva.getId(), "CRIADA",
                null, reserva.getStatus().name(), detalhesReserva("CRIADA", null, reserva));
        return reserva;
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

    @Transactional(noRollbackFor = ReservaConflitoException.class)
    public Reserva atualizar(Long id, String emailAutenticado, ReservaRequest request) {
        Usuario usuario = usuarioSolicitante(emailAutenticado);
        Reserva reserva = reservaRepository.findByIdParaAtualizacao(id).orElseThrow(ReservaNotFoundException::new);
        verificarProprietario(reserva, usuario);
        StatusReserva estadoAnterior = reserva.getStatus();
        Map<String, Object> snapshotAnterior = snapshotReserva(reserva);
        Recurso recurso = bloquearRecursosParaAtualizacao(reserva, request);
        if (STATUS_QUE_BLOQUEIAM_HORARIO.contains(estadoAnterior)) {
            verificarConflito(emailAutenticado, recurso, request, reserva.getId());
        }
        StatusReserva status = estadoAnterior == StatusReserva.SOLICITADA
                || estadoAnterior == StatusReserva.APROVADA ? statusInicial(recurso) : estadoAnterior;
        reserva.atualizar(recurso.sala(), recurso.professor(), recurso.material(), request.inicio(), request.termino(), status);
        Reserva salva = reservaRepository.save(reserva);
        auditoriaService.registrar(emailAutenticado, "RESERVA", salva.getId(), "ALTERADA",
                estadoAnterior.name(), salva.getStatus().name(), detalhesReserva("ALTERADA", snapshotAnterior, salva));
        return salva;
    }

    @Transactional(noRollbackFor = ReservaConflitoException.class)
    public Reserva aprovar(Long id, String emailAutenticado) {
        usuarioResponsavel(emailAutenticado);
        Reserva reserva = reservaRepository.findByIdParaAtualizacao(id).orElseThrow(ReservaNotFoundException::new);
        validarSolicitacaoRestritaPendente(reserva);
        Recurso recurso = bloquearRecursoDaReserva(reserva);
        verificarConflito(emailAutenticado, recurso, reserva.getInicio(), reserva.getTermino(), reserva.getId());
        StatusReserva anterior = reserva.getStatus();
        Map<String, Object> snapshotAnterior = snapshotReserva(reserva);
        reserva.aprovar();
        Reserva salva = reservaRepository.save(reserva);
        auditoriaService.registrar(emailAutenticado, "RESERVA", salva.getId(), "APROVADA",
                anterior.name(), salva.getStatus().name(), detalhesReserva("APROVADA", snapshotAnterior, salva));
        notificacaoService.criarParaUsuario(salva.getSolicitante().getEmail(), "RESERVA_APROVADA",
                "Sua reserva foi aprovada.", "RESERVA", salva.getId());
        return salva;
    }

    @Transactional
    public Reserva rejeitar(Long id, String emailAutenticado) {
        usuarioResponsavel(emailAutenticado);
        Reserva reserva = reservaRepository.findByIdParaAtualizacao(id).orElseThrow(ReservaNotFoundException::new);
        validarSolicitacaoRestritaPendente(reserva);
        StatusReserva anterior = reserva.getStatus();
        Map<String, Object> snapshotAnterior = snapshotReserva(reserva);
        reserva.rejeitar();
        Reserva salva = reservaRepository.save(reserva);
        auditoriaService.registrar(emailAutenticado, "RESERVA", salva.getId(), "REJEITADA",
                anterior.name(), salva.getStatus().name(), detalhesReserva("REJEITADA", snapshotAnterior, salva));
        notificacaoService.criarParaUsuario(salva.getSolicitante().getEmail(), "RESERVA_REJEITADA",
                "Sua reserva foi rejeitada.", "RESERVA", salva.getId());
        return salva;
    }

    @Transactional
    public Reserva registrarRetirada(Long id, String emailAutenticado) {
        usuarioResponsavelOuAdmin(emailAutenticado);
        Reserva reserva = reservaRepository.findByIdParaAtualizacao(id).orElseThrow(ReservaNotFoundException::new);
        if (reserva.getStatus() != StatusReserva.APROVADA) {
            throw new ReservaEstadoInvalidoException();
        }
        if (reserva.getRetiradoEm() != null) {
            throw new ReservaEstadoInvalidoException();
        }
        StatusReserva anterior = reserva.getStatus();
        Map<String, Object> snapshotAnterior = snapshotReserva(reserva);
        reserva.registrarRetirada(java.time.OffsetDateTime.now());
        Reserva salva = reservaRepository.save(reserva);
        auditoriaService.registrar(emailAutenticado, "RESERVA", salva.getId(), "RETIRADA",
                anterior.name(), salva.getStatus().name(), detalhesReserva("RETIRADA", snapshotAnterior, salva));
        notificacaoService.criarParaUsuario(salva.getSolicitante().getEmail(), "RESERVA_RETIRADA",
                "A retirada do recurso foi registrada.", "RESERVA", salva.getId());
        return salva;
    }

    @Transactional
    public Reserva registrarDevolucao(Long id, String emailAutenticado) {
        usuarioResponsavelOuAdmin(emailAutenticado);
        Reserva reserva = reservaRepository.findByIdParaAtualizacao(id).orElseThrow(ReservaNotFoundException::new);
        if (reserva.getStatus() != StatusReserva.EM_USO) {
            throw new ReservaEstadoInvalidoException();
        }
        if (reserva.getRetiradoEm() == null) {
            throw new ReservaEstadoInvalidoException();
        }
        if (reserva.getDevolvidoEm() != null) {
            throw new ReservaEstadoInvalidoException();
        }
        StatusReserva anterior = reserva.getStatus();
        Map<String, Object> snapshotAnterior = snapshotReserva(reserva);
        reserva.registrarDevolucao(java.time.OffsetDateTime.now());
        Reserva salva = reservaRepository.save(reserva);
        auditoriaService.registrar(emailAutenticado, "RESERVA", salva.getId(), "DEVOLVIDA",
                anterior.name(), salva.getStatus().name(), detalhesReserva("DEVOLVIDA", snapshotAnterior, salva));
        notificacaoService.criarParaUsuario(salva.getSolicitante().getEmail(), "RESERVA_DEVOLVIDA",
                "A devolucao do recurso foi registrada.", "RESERVA", salva.getId());
        return salva;
    }

    @Transactional
    public Reserva registrarNaoComparecimento(Long id, String emailAutenticado) {
        usuarioResponsavelOuAdmin(emailAutenticado);
        Reserva reserva = reservaRepository.findByIdParaAtualizacao(id).orElseThrow(ReservaNotFoundException::new);
        if (reserva.getStatus() != StatusReserva.APROVADA) {
            throw new ReservaEstadoInvalidoException();
        }
        StatusReserva anterior = reserva.getStatus();
        Map<String, Object> snapshotAnterior = snapshotReserva(reserva);
        reserva.registrarNaoComparecimento();
        Reserva salva = reservaRepository.save(reserva);
        auditoriaService.registrar(emailAutenticado, "RESERVA", salva.getId(), "NAO_COMPARECEU",
                anterior.name(), salva.getStatus().name(), detalhesReserva("NAO_COMPARECEU", snapshotAnterior, salva));
        notificacaoService.criarParaUsuario(salva.getSolicitante().getEmail(), "RESERVA_NAO_COMPARECEU",
                "O nao comparecimento da reserva foi registrado.", "RESERVA", salva.getId());
        return salva;
    }

    @Transactional
    public void cancelar(Long id, String emailAutenticado) {
        Usuario usuario = usuarioSolicitante(emailAutenticado);
        Reserva reserva = reservaRepository.findByIdParaAtualizacao(id).orElseThrow(ReservaNotFoundException::new);
        verificarProprietario(reserva, usuario);
        if (reserva.getStatus() != StatusReserva.SOLICITADA
                && reserva.getStatus() != StatusReserva.APROVADA) {
            throw new ReservaEstadoInvalidoException();
        }
        StatusReserva anterior = reserva.getStatus();
        Map<String, Object> snapshotAnterior = snapshotReserva(reserva);
        reserva.cancelar();
        Reserva salva = reservaRepository.save(reserva);
        auditoriaService.registrar(emailAutenticado, "RESERVA", salva.getId(), "CANCELADA",
                anterior.name(), salva.getStatus().name(), detalhesReserva("CANCELADA", snapshotAnterior, salva));
        notificacaoService.criarParaUsuario(salva.getSolicitante().getEmail(), "RESERVA_CANCELADA",
                "Sua reserva foi cancelada.", "RESERVA", salva.getId());
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

    private void usuarioResponsavelOuAdmin(String email) {
        Usuario usuario = usuarioAtivo(email);
        if (usuario.getPerfil() != Perfil.RESPONSAVEL && usuario.getPerfil() != Perfil.ADMIN) {
            throw new ReservaAprovacaoNaoPermitidaException();
        }
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

    private void verificarConflito(String emailAutenticado, Recurso recurso, ReservaRequest request, Long idExcluido) {
        verificarConflito(emailAutenticado, recurso, request.inicio(), request.termino(), idExcluido);
    }

    private void verificarConflito(String emailAutenticado, Recurso recurso, java.time.OffsetDateTime inicio,
                                   java.time.OffsetDateTime termino, Long idExcluido) {
        Long salaId = recurso.sala() == null ? null : recurso.sala().getId();
        Long professorId = recurso.professor() == null ? null : recurso.professor().getId();
        Long materialId = recurso.material() == null ? null : recurso.material().getId();
        boolean conflitoReserva = reservaRepository.existsConflito(salaId, professorId, materialId,
                inicio, termino, STATUS_QUE_BLOQUEIAM_HORARIO, idExcluido);
        boolean conflitoManutencao = manutencaoRepository.existsConflito(salaId, professorId, materialId,
                inicio, termino, StatusManutencao.ATIVA, null);
        if (conflitoReserva || conflitoManutencao) {
            Map<String, Object> detalhes = new LinkedHashMap<>();
            detalhes.put("operacao", "CONFLITO_EVITADO");
            detalhes.put("origem", "RESERVA");
            detalhes.put("recurso", snapshotRecurso(recurso));
            detalhes.put("inicio", inicio);
            detalhes.put("termino", termino);
            detalhes.put("conflitoReserva", conflitoReserva);
            detalhes.put("conflitoManutencao", conflitoManutencao);
            auditoriaService.registrarConflitoEvitado(emailAutenticado, tipoRecurso(recurso), idRecurso(recurso),
                    detalhes);
            throw new ReservaConflitoException();
        }
    }

    private Map<String, Object> detalhesReserva(String operacao, Map<String, Object> anterior, Reserva posterior) {
        Map<String, Object> detalhes = new LinkedHashMap<>();
        detalhes.put("operacao", operacao);
        detalhes.put("antes", anterior);
        detalhes.put("depois", snapshotReserva(posterior));
        return detalhes;
    }

    private Map<String, Object> snapshotReserva(Reserva reserva) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("reservaId", reserva.getId());
        snapshot.put("status", reserva.getStatus());
        snapshot.put("recurso", snapshotRecurso(reserva));
        snapshot.put("inicio", reserva.getInicio());
        snapshot.put("termino", reserva.getTermino());
        snapshot.put("retiradoEm", reserva.getRetiradoEm());
        snapshot.put("devolvidoEm", reserva.getDevolvidoEm());
        return snapshot;
    }

    private Map<String, Object> snapshotRecurso(Reserva reserva) {
        if (reserva.getSala() != null) return snapshotRecurso(reserva.getSala());
        if (reserva.getProfessor() != null) return snapshotRecurso(reserva.getProfessor());
        return snapshotRecurso(reserva.getMaterial());
    }

    private Map<String, Object> snapshotRecurso(Recurso recurso) {
        if (recurso.sala() != null) return snapshotRecurso(recurso.sala());
        if (recurso.professor() != null) return snapshotRecurso(recurso.professor());
        return snapshotRecurso(recurso.material());
    }

    private Map<String, Object> snapshotRecurso(Sala sala) {
        Map<String, Object> recurso = new LinkedHashMap<>();
        recurso.put("tipo", "SALA");
        recurso.put("id", sala.getId());
        recurso.put("nome", sala.getNome());
        recurso.put("categoria", sala.getTipo());
        return recurso;
    }

    private Map<String, Object> snapshotRecurso(Professor professor) {
        Map<String, Object> recurso = new LinkedHashMap<>();
        recurso.put("tipo", "PROFESSOR");
        recurso.put("id", professor.getId());
        recurso.put("nome", professor.getNome());
        recurso.put("categoria", professor.getCompetencia());
        return recurso;
    }

    private Map<String, Object> snapshotRecurso(Material material) {
        Map<String, Object> recurso = new LinkedHashMap<>();
        recurso.put("tipo", "MATERIAL");
        recurso.put("id", material.getId());
        recurso.put("nome", material.getNome());
        recurso.put("categoria", material.getTipo());
        return recurso;
    }

    private String tipoRecurso(Recurso recurso) {
        if (recurso.sala() != null) return "SALA";
        if (recurso.professor() != null) return "PROFESSOR";
        return "MATERIAL";
    }

    private Long idRecurso(Recurso recurso) {
        if (recurso.sala() != null) return recurso.sala().getId();
        if (recurso.professor() != null) return recurso.professor().getId();
        return recurso.material().getId();
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
