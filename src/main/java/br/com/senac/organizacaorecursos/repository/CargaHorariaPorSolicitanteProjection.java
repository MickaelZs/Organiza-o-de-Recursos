package br.com.senac.organizacaorecursos.repository;

import java.math.BigDecimal;

public interface CargaHorariaPorSolicitanteProjection {
    Long getSolicitanteId();
    String getSolicitanteEmail();
    Long getQuantidadeReservas();
    BigDecimal getHorasReservadas();
}
