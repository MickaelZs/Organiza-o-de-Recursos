package br.com.senac.organizacaorecursos.service;

import br.com.senac.organizacaorecursos.dto.CargaHorariaPorSolicitanteResponse;
import br.com.senac.organizacaorecursos.dto.CargaHorariaPorProfessorResponse;
import br.com.senac.organizacaorecursos.dto.CargaHorariaRelatorioResponse;
import br.com.senac.organizacaorecursos.dto.ConflitosEvitadosRelatorioResponse;
import br.com.senac.organizacaorecursos.dto.UtilizacaoPorTipoResponse;
import br.com.senac.organizacaorecursos.dto.UtilizacaoPorRecursoResponse;
import br.com.senac.organizacaorecursos.dto.UtilizacaoRelatorioResponse;
import br.com.senac.organizacaorecursos.repository.AuditoriaRepository;
import br.com.senac.organizacaorecursos.repository.CargaHorariaPorSolicitanteProjection;
import br.com.senac.organizacaorecursos.repository.CargaHorariaPorProfessorProjection;
import br.com.senac.organizacaorecursos.repository.RelatorioRepository;
import br.com.senac.organizacaorecursos.repository.UtilizacaoPorTipoProjection;
import br.com.senac.organizacaorecursos.repository.UtilizacaoPorRecursoProjection;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RelatorioService {
    private static final String CONFLITO_EVITADO = "CONFLITO_EVITADO";

    private final RelatorioRepository relatorioRepository;
    private final AuditoriaRepository auditoriaRepository;

    public RelatorioService(RelatorioRepository relatorioRepository, AuditoriaRepository auditoriaRepository) {
        this.relatorioRepository = relatorioRepository;
        this.auditoriaRepository = auditoriaRepository;
    }

    @Transactional(readOnly = true)
    public UtilizacaoRelatorioResponse utilizacao(java.time.OffsetDateTime inicio,
                                                  java.time.OffsetDateTime termino) {
        validarPeriodo(inicio, termino);
        List<UtilizacaoPorTipoResponse> itens = relatorioRepository.buscarUtilizacaoPorTipo(inicio, termino)
                .stream().map(this::utilizacaoResponse).toList();
        List<UtilizacaoPorRecursoResponse> recursos = relatorioRepository
                .buscarUtilizacaoPorRecurso(inicio, termino).stream()
                .map(this::utilizacaoPorRecursoResponse).toList();
        return new UtilizacaoRelatorioResponse(inicio, termino,
                itens.stream().mapToLong(UtilizacaoPorTipoResponse::quantidadeReservas).sum(),
                itens.stream().map(UtilizacaoPorTipoResponse::horasUtilizadas)
                        .reduce(BigDecimal.ZERO, BigDecimal::add), itens, recursos);
    }

    @Transactional(readOnly = true)
    public CargaHorariaRelatorioResponse cargaHoraria(java.time.OffsetDateTime inicio,
                                                      java.time.OffsetDateTime termino) {
        validarPeriodo(inicio, termino);
        List<CargaHorariaPorSolicitanteResponse> itens = relatorioRepository
                .buscarCargaHorariaPorSolicitante(inicio, termino).stream()
                .map(this::cargaHorariaResponse).toList();
        BigDecimal total = itens.stream().map(CargaHorariaPorSolicitanteResponse::horasReservadas)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<CargaHorariaPorProfessorResponse> professores = relatorioRepository
                .buscarCargaHorariaPorProfessor(inicio, termino).stream()
                .map(this::cargaHorariaPorProfessorResponse).toList();
        BigDecimal totalAlocado = professores.stream().map(CargaHorariaPorProfessorResponse::horasAlocadas)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CargaHorariaRelatorioResponse(inicio, termino, total, itens, totalAlocado, professores);
    }

    @Transactional(readOnly = true)
    public ConflitosEvitadosRelatorioResponse conflitosEvitados(java.time.OffsetDateTime inicio,
                                                                java.time.OffsetDateTime termino) {
        validarPeriodo(inicio, termino);
        return new ConflitosEvitadosRelatorioResponse(inicio, termino,
                auditoriaRepository.countByAcaoAndCriadoEmGreaterThanEqualAndCriadoEmLessThan(
                        CONFLITO_EVITADO, inicio, termino));
    }

    private void validarPeriodo(java.time.OffsetDateTime inicio, java.time.OffsetDateTime termino) {
        if (inicio == null || termino == null || !termino.isAfter(inicio)) {
            throw new PeriodoRelatorioInvalidoException();
        }
    }

    private UtilizacaoPorTipoResponse utilizacaoResponse(UtilizacaoPorTipoProjection projection) {
        return new UtilizacaoPorTipoResponse(projection.getTipoRecurso(), projection.getQuantidadeReservas(),
                projection.getHorasUtilizadas());
    }

    private CargaHorariaPorSolicitanteResponse cargaHorariaResponse(CargaHorariaPorSolicitanteProjection projection) {
        return new CargaHorariaPorSolicitanteResponse(projection.getSolicitanteId(),
                projection.getSolicitanteEmail(), projection.getQuantidadeReservas(), projection.getHorasReservadas());
    }

    private UtilizacaoPorRecursoResponse utilizacaoPorRecursoResponse(UtilizacaoPorRecursoProjection projection) {
        return new UtilizacaoPorRecursoResponse(projection.getTipoRecurso(), projection.getRecursoId(),
                projection.getNomeRecurso(), projection.getCategoria(), projection.getQuantidadeReservas(),
                projection.getHorasUtilizadas());
    }

    private CargaHorariaPorProfessorResponse cargaHorariaPorProfessorResponse(
            CargaHorariaPorProfessorProjection projection) {
        return new CargaHorariaPorProfessorResponse(projection.getProfessorId(), projection.getProfessorNome(),
                projection.getCompetencia(), projection.getQuantidadeReservas(), projection.getHorasAlocadas());
    }
}
