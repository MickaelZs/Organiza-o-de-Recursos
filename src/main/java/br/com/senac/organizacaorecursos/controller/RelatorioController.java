package br.com.senac.organizacaorecursos.controller;

import br.com.senac.organizacaorecursos.dto.CargaHorariaRelatorioResponse;
import br.com.senac.organizacaorecursos.dto.ConflitosEvitadosRelatorioResponse;
import br.com.senac.organizacaorecursos.dto.UtilizacaoRelatorioResponse;
import br.com.senac.organizacaorecursos.service.RelatorioService;
import java.time.OffsetDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {
    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/utilizacao")
    public UtilizacaoRelatorioResponse utilizacao(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime termino) {
        return relatorioService.utilizacao(inicio, termino);
    }

    @GetMapping("/carga-horaria")
    public CargaHorariaRelatorioResponse cargaHoraria(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime termino) {
        return relatorioService.cargaHoraria(inicio, termino);
    }

    @GetMapping("/conflitos-evitados")
    public ConflitosEvitadosRelatorioResponse conflitosEvitados(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime termino) {
        return relatorioService.conflitosEvitados(inicio, termino);
    }
}
