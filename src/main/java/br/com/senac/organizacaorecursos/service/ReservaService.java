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
import java.util.EnumSet;
import java.util.List;
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
                request.inicio(), request.termino(), StatusReserva.SOLICITADA));
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
        Reserva reserva = reservaRepository.findById(id).orElseThrow(ReservaNotFoundException::new);
        verificarProprietario(reserva, usuario);
        var recurso = resolverRecurso(request);
        if (STATUS_QUE_BLOQUEIAM_HORARIO.contains(reserva.getStatus())) {
            verificarConflito(recurso, request, reserva.getId());
        }
        reserva.atualizar(recurso.sala(), recurso.professor(), recurso.material(), request.inicio(), request.termino());
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

    private void verificarConflito(Recurso recurso, ReservaRequest request, Long idExcluido) {
        Long salaId = recurso.sala() == null ? null : recurso.sala().getId();
        Long professorId = recurso.professor() == null ? null : recurso.professor().getId();
        Long materialId = recurso.material() == null ? null : recurso.material().getId();
        if (reservaRepository.existsConflito(salaId, professorId, materialId,
                request.inicio(), request.termino(), STATUS_QUE_BLOQUEIAM_HORARIO, idExcluido)) {
            throw new ReservaConflitoException();
        }
    }

    private record Recurso(Sala sala, Professor professor, Material material) { }
}
