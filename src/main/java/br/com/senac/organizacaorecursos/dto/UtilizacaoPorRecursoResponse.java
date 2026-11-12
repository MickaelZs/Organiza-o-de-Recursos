package br.com.senac.organizacaorecursos.dto;

import java.math.BigDecimal;

public record UtilizacaoPorRecursoResponse(
        String tipoRecurso,
        Long recursoId,
        String nomeRecurso,
        String categoria,
        Long quantidadeReservas,
        BigDecimal horasUtilizadas) { }
