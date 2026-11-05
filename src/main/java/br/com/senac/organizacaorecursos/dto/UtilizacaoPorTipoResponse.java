package br.com.senac.organizacaorecursos.dto;

import java.math.BigDecimal;

public record UtilizacaoPorTipoResponse(
        String tipoRecurso,
        Long quantidadeReservas,
        BigDecimal horasUtilizadas) { }
