package br.com.senac.organizacaorecursos.controller;

import br.com.senac.organizacaorecursos.dto.MaterialCreateRequest;
import br.com.senac.organizacaorecursos.dto.MaterialResponse;
import br.com.senac.organizacaorecursos.service.MaterialService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/materiais")
public class MaterialController {
    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @PostMapping
    public ResponseEntity<MaterialResponse> criar(@Valid @RequestBody MaterialCreateRequest request) {
        MaterialResponse response = MaterialResponse.from(materialService.criar(request));
        return ResponseEntity.created(URI.create("/api/materiais/" + response.id())).body(response);
    }

    @GetMapping
    public List<MaterialResponse> listar(@RequestParam(required = false) String tipo,
                                         @RequestParam(required = false) String localizacao) {
        return materialService.buscar(tipo, localizacao).stream().map(MaterialResponse::from).toList();
    }

    @GetMapping("/{id}")
    public MaterialResponse buscarPorId(@PathVariable Long id) {
        return MaterialResponse.from(materialService.buscarPorId(id));
    }
}
