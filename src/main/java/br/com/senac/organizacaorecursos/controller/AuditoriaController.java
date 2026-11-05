package br.com.senac.organizacaorecursos.controller;

import br.com.senac.organizacaorecursos.entity.Auditoria;
import br.com.senac.organizacaorecursos.service.AuditoriaService;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/auditorias", "/api/auditoria"})
public class AuditoriaController {
    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public List<Auditoria> listar(Authentication authentication) {
        return auditoriaService.listar(authentication.getName());
    }
}
