package br.com.senac.organizacaorecursos.dto;

import java.math.BigDecimal;

public record CargaHorariaPorSolicitanteResponse(
        Long solicitanteId,
        String solicitanteEmail,
        Long quantidadeReservas,
        BigDecimal horasReservadas) { }
