package br.com.senac.organizacaorecursos.dto;

import java.time.OffsetDateTime;

public record ConflitosEvitadosRelatorioResponse(
        OffsetDateTime inicio,
        OffsetDateTime termino,
        Long totalConflitosEvitados) { }
