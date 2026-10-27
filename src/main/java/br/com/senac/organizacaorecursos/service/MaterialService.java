package br.com.senac.organizacaorecursos.service;

import br.com.senac.organizacaorecursos.dto.MaterialCreateRequest;
import br.com.senac.organizacaorecursos.entity.Material;
import br.com.senac.organizacaorecursos.repository.MaterialRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaterialService {
    private final MaterialRepository materialRepository;

    public MaterialService(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    @Transactional
    public Material criar(MaterialCreateRequest request) {
        if (request.quantidadeTotal() == null || request.quantidadeDisponivel() == null
                || request.quantidadeDisponivel() > request.quantidadeTotal()) {
            throw new InvalidMaterialQuantityException();
        }
        return materialRepository.save(new Material(request.nome(), request.tipo(), request.quantidadeTotal(),
                request.quantidadeDisponivel(), request.localizacao(), request.restrito(), request.ativo()));
    }

    @Transactional(readOnly = true)
    public List<Material> listar() {
        return materialRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Material buscarPorId(Long id) {
        return materialRepository.findById(id).orElseThrow(MaterialNotFoundException::new);
    }
}
