package br.com.senac.organizacaorecursos.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record CargaHorariaRelatorioResponse(
        OffsetDateTime inicio,
        OffsetDateTime termino,
        BigDecimal totalHorasReservadas,
        List<CargaHorariaPorSolicitanteResponse> porSolicitante,
        BigDecimal totalHorasAlocadas,
        List<CargaHorariaPorProfessorResponse> porProfessor) { }
