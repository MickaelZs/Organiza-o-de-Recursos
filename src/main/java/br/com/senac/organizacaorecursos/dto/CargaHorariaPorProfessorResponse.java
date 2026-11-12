package br.com.senac.organizacaorecursos.dto;

import java.math.BigDecimal;

public record CargaHorariaPorProfessorResponse(
        Long professorId,
        String professorNome,
        String competencia,
        Long quantidadeReservas,
        BigDecimal horasAlocadas) { }
