package br.com.senac.organizacaorecursos.service;

import br.com.senac.organizacaorecursos.dto.SalaCreateRequest;
import br.com.senac.organizacaorecursos.entity.Sala;
import br.com.senac.organizacaorecursos.repository.SalaRepository;
import java.util.List;
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
    public Sala buscarPorId(Long id) {
        return salaRepository.findById(id).orElseThrow(SalaNotFoundException::new);
    }
}
