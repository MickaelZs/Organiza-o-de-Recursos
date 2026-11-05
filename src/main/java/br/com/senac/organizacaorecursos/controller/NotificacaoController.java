package br.com.senac.organizacaorecursos.controller;

import br.com.senac.organizacaorecursos.entity.Notificacao;
import br.com.senac.organizacaorecursos.service.NotificacaoService;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/notificacoes", "/api/notificacao"})
public class NotificacaoController {
    private final NotificacaoService notificacaoService;

    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @GetMapping
    public List<Notificacao> listar(Authentication authentication) {
        return notificacaoService.listar(authentication.getName());
    }

    @PatchMapping("/{id}/lida")
    public Notificacao marcarComoLida(@PathVariable Long id, Authentication authentication) {
        return notificacaoService.marcarComoLida(id, authentication.getName());
    }
}
