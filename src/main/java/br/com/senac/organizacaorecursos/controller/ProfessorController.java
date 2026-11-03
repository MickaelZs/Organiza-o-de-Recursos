package br.com.senac.organizacaorecursos.controller;

import br.com.senac.organizacaorecursos.dto.ProfessorCreateRequest;
import br.com.senac.organizacaorecursos.dto.ProfessorResponse;
import br.com.senac.organizacaorecursos.service.ProfessorService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.time.OffsetDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/api/professores")
public class ProfessorController {
    private final ProfessorService professorService;

    public ProfessorController(ProfessorService professorService) {
        this.professorService = professorService;
    }

    @PostMapping
    public ResponseEntity<ProfessorResponse> criar(@Valid @RequestBody ProfessorCreateRequest request) {
        ProfessorResponse response = ProfessorResponse.from(professorService.criar(request));
        return ResponseEntity.created(URI.create("/api/professores/" + response.id())).body(response);
    }

    @GetMapping
    public List<ProfessorResponse> listar(@RequestParam(required = false) String competencia,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                  OffsetDateTime inicio,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                  OffsetDateTime termino) {
        return professorService.buscarPorCompetencia(competencia, inicio, termino)
                .stream().map(ProfessorResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ProfessorResponse buscarPorId(@PathVariable Long id) {
        return ProfessorResponse.from(professorService.buscarPorId(id));
    }
}
