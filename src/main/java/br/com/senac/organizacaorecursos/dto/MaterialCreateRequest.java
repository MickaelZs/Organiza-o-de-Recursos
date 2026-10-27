package br.com.senac.organizacaorecursos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MaterialCreateRequest(
        @NotBlank String nome,
        @NotBlank String tipo,
        @NotNull @Min(0) Integer quantidadeTotal,
        @NotNull @Min(0) Integer quantidadeDisponivel,
        @NotBlank String localizacao,
        @NotNull Boolean restrito,
        @NotNull Boolean ativo) { }
