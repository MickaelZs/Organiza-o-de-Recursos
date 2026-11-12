package br.com.senac.organizacaorecursos.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record UtilizacaoRelatorioResponse(
        OffsetDateTime inicio,
        OffsetDateTime termino,
        Long totalReservas,
        BigDecimal totalHorasUtilizadas,
        List<UtilizacaoPorTipoResponse> porTipoRecurso,
        List<UtilizacaoPorRecursoResponse> porRecurso) { }
