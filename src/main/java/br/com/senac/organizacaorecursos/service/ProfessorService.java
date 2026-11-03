package br.com.senac.organizacaorecursos.service;

import br.com.senac.organizacaorecursos.dto.ProfessorCreateRequest;
import br.com.senac.organizacaorecursos.entity.Professor;
import br.com.senac.organizacaorecursos.repository.ProfessorRepository;
import java.util.List;
import java.util.Locale;
import java.time.OffsetDateTime;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfessorService {
    private final ProfessorRepository professorRepository;

    public ProfessorService(ProfessorRepository professorRepository) {
        this.professorRepository = professorRepository;
    }

    @Transactional
    public Professor criar(ProfessorCreateRequest request) {
        if (professorRepository.existsByEmail(request.email())) {
            throw new DuplicateProfessorEmailException();
        }
        return professorRepository.save(new Professor(request.nome(), request.email(), request.competencia(), request.ativo()));
    }

    @Transactional(readOnly = true)
    public List<Professor> listar() {
        return professorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Professor> buscarPorCompetencia(String competencia) {
        return buscarPorCompetencia(competencia, null, null);
    }

    @Transactional(readOnly = true)
    public List<Professor> buscarPorCompetencia(String competencia, OffsetDateTime inicio, OffsetDateTime termino) {
        validarIntervalo(inicio, termino);
        if (competencia == null || competencia.isBlank()) {
            Specification<Professor> filtros = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
            if (inicio != null) filtros = filtros.and(DisponibilidadeSpecifications.disponivel("professor", inicio, termino));
            return professorRepository.findAll(filtros);
        }
        String termo = "%" + competencia.trim().toLowerCase(Locale.ROOT) + "%";
        Specification<Professor> filtro = (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("competencia")), termo);
        if (inicio != null) filtro = filtro.and(DisponibilidadeSpecifications.disponivel("professor", inicio, termino));
        return professorRepository.findAll(filtro);
    }

    private void validarIntervalo(OffsetDateTime inicio, OffsetDateTime termino) {
        if ((inicio == null) != (termino == null) || (inicio != null && !termino.isAfter(inicio))) {
            throw new IntervaloDisponibilidadeInvalidoException();
        }
    }

    @Transactional(readOnly = true)
    public Professor buscarPorId(Long id) {
        return professorRepository.findById(id).orElseThrow(ProfessorNotFoundException::new);
    }
}
