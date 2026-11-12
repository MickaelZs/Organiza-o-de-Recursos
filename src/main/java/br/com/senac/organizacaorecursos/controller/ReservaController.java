package br.com.senac.organizacaorecursos.controller;

import br.com.senac.organizacaorecursos.dto.ReservaRequest;
import br.com.senac.organizacaorecursos.dto.ReservaResponse;
import br.com.senac.organizacaorecursos.service.ReservaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {
    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) { this.reservaService = reservaService; }

    @PostMapping
    public ResponseEntity<ReservaResponse> criar(Authentication authentication,
                                                 @Valid @RequestBody ReservaRequest request) {
        ReservaResponse response = ReservaResponse.from(reservaService.criar(authentication.getName(), request));
        return ResponseEntity.created(URI.create("/api/reservas/" + response.id())).body(response);
    }

    @PostMapping("/{id}/aprovar")
    public ReservaResponse aprovar(@PathVariable Long id, Authentication authentication) {
        return ReservaResponse.from(reservaService.aprovar(id, authentication.getName()));
    }

    @PostMapping("/{id}/rejeitar")
    public ReservaResponse rejeitar(@PathVariable Long id, Authentication authentication) {
        return ReservaResponse.from(reservaService.rejeitar(id, authentication.getName()));
    }

    @PostMapping("/{id}/retirada")
    public ReservaResponse registrarRetirada(@PathVariable Long id, Authentication authentication) {
        return ReservaResponse.from(reservaService.registrarRetirada(id, authentication.getName()));
    }

    @PostMapping("/{id}/devolucao")
    public ReservaResponse registrarDevolucao(@PathVariable Long id, Authentication authentication) {
        return ReservaResponse.from(reservaService.registrarDevolucao(id, authentication.getName()));
    }

    @PostMapping("/{id}/nao-compareceu")
    public ReservaResponse registrarNaoComparecimento(@PathVariable Long id, Authentication authentication) {
        return ReservaResponse.from(reservaService.registrarNaoComparecimento(id, authentication.getName()));
    }

    @GetMapping
    public List<ReservaResponse> listar(Authentication authentication) {
        return reservaService.listar(authentication.getName()).stream().map(ReservaResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ReservaResponse buscarPorId(@PathVariable Long id, Authentication authentication) {
        return ReservaResponse.from(reservaService.buscarPorId(id, authentication.getName()));
    }

    @PutMapping("/{id}")
    public ReservaResponse atualizar(@PathVariable Long id, Authentication authentication,
                                     @Valid @RequestBody ReservaRequest request) {
        return ReservaResponse.from(reservaService.atualizar(id, authentication.getName(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id, Authentication authentication) {
        reservaService.cancelar(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
