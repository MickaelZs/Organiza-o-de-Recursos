package br.com.senac.organizacaorecursos.repository;

import java.math.BigDecimal;

public interface UtilizacaoPorRecursoProjection {
    String getTipoRecurso();
    Long getRecursoId();
    String getNomeRecurso();
    String getCategoria();
    Long getQuantidadeReservas();
    BigDecimal getHorasUtilizadas();
}
