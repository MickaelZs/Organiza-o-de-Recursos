package br.com.senac.organizacaorecursos.repository;

import java.math.BigDecimal;

public interface CargaHorariaPorProfessorProjection {
    Long getProfessorId();
    String getProfessorNome();
    String getCompetencia();
    Long getQuantidadeReservas();
    BigDecimal getHorasAlocadas();
}
