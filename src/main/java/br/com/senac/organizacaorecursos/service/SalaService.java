package br.com.senac.organizacaorecursos.service;

import br.com.senac.organizacaorecursos.dto.SalaCreateRequest;
import br.com.senac.organizacaorecursos.entity.Sala;
import br.com.senac.organizacaorecursos.repository.SalaRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SalaService {
    private final SalaRepository salaRepository;

    public SalaService(SalaRepository salaRepository) {
        this.salaRepository = salaRepository;
    }

    @Transactional
    public Sala criar(SalaCreateRequest request) {
        return salaRepository.save(new Sala(request.nome(), request.tipo(), request.capacidade(),
                request.localizacao(), request.restrita(), request.ativa()));
    }

    @Transactional(readOnly = true)
    public List<Sala> listar() {
        return salaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Sala> buscar(String tipo, Integer capacidadeMinima, String localizacao) {
        Specification<Sala> filtros = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        if (temTexto(tipo)) {
            filtros = filtros.and(contemIgnorandoMaiusculas("tipo", tipo));
        }
        if (capacidadeMinima != null) {
            filtros = filtros.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.<Integer>get("capacidade"), capacidadeMinima));
        }
        if (temTexto(localizacao)) {
            filtros = filtros.and(contemIgnorandoMaiusculas("localizacao", localizacao));
        }
        return salaRepository.findAll(filtros);
    }

    @Transactional(readOnly = true)
    public Sala buscarPorId(Long id) {
        return salaRepository.findById(id).orElseThrow(SalaNotFoundException::new);
    }

    private Specification<Sala> contemIgnorandoMaiusculas(String atributo, String valor) {
        String termo = "%" + valor.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get(atributo)), termo);
    }

    private boolean temTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
