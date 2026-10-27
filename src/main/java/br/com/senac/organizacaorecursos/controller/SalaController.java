package br.com.senac.organizacaorecursos.controller;

import br.com.senac.organizacaorecursos.dto.SalaCreateRequest;
import br.com.senac.organizacaorecursos.dto.SalaResponse;
import br.com.senac.organizacaorecursos.service.SalaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/salas")
public class SalaController {
    private final SalaService salaService;

    public SalaController(SalaService salaService) {
        this.salaService = salaService;
    }

    @PostMapping
    public ResponseEntity<SalaResponse> criar(@Valid @RequestBody SalaCreateRequest request) {
        SalaResponse response = SalaResponse.from(salaService.criar(request));
        return ResponseEntity.created(URI.create("/api/salas/" + response.id())).body(response);
    }

    @GetMapping
    public List<SalaResponse> listar() {
        return salaService.listar().stream().map(SalaResponse::from).toList();
    }

    @GetMapping("/{id}")
    public SalaResponse buscarPorId(@PathVariable Long id) {
        return SalaResponse.from(salaService.buscarPorId(id));
    }
}
