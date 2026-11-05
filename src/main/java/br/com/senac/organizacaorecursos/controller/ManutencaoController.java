package br.com.senac.organizacaorecursos.controller;

import br.com.senac.organizacaorecursos.dto.ManutencaoRequest;
import br.com.senac.organizacaorecursos.dto.ManutencaoResponse;
import br.com.senac.organizacaorecursos.service.ManutencaoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/manutencoes")
@Profile("!test")
public class ManutencaoController {
    private final ManutencaoService manutencaoService;

    public ManutencaoController(ManutencaoService manutencaoService) {
        this.manutencaoService = manutencaoService;
    }

    @PostMapping
    public ResponseEntity<ManutencaoResponse> criar(Authentication authentication,
                                                   @Valid @RequestBody ManutencaoRequest request) {
        ManutencaoResponse response = ManutencaoResponse.from(manutencaoService.criar(authentication.getName(), request));
        return ResponseEntity.created(URI.create("/api/manutencoes/" + response.id())).body(response);
    }

    @GetMapping
    public List<ManutencaoResponse> listar(Authentication authentication) {
        return manutencaoService.listar(authentication.getName()).stream().map(ManutencaoResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ManutencaoResponse buscarPorId(@PathVariable Long id, Authentication authentication) {
        return ManutencaoResponse.from(manutencaoService.buscarPorId(id, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id, Authentication authentication) {
        manutencaoService.cancelar(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
