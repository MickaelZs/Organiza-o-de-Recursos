package br.com.senac.organizacaorecursos.service;

import br.com.senac.organizacaorecursos.dto.ProfessorCreateRequest;
import br.com.senac.organizacaorecursos.entity.Professor;
import br.com.senac.organizacaorecursos.repository.ProfessorRepository;
import java.util.List;
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
    public Professor buscarPorId(Long id) {
        return professorRepository.findById(id).orElseThrow(ProfessorNotFoundException::new);
    }
}
