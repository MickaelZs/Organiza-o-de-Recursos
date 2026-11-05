package br.com.senac.organizacaorecursos.repository;

import java.math.BigDecimal;

public interface UtilizacaoPorTipoProjection {
    String getTipoRecurso();
    Long getQuantidadeReservas();
    BigDecimal getHorasUtilizadas();
}
