package br.com.senac.organizacaorecursos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SalaCreateRequest(
        @NotBlank String nome,
        @NotBlank String tipo,
        @NotNull @Positive Integer capacidade,
        @NotBlank String localizacao,
        @NotNull Boolean restrita,
        @NotNull Boolean ativa) { }
