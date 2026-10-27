package br.com.senac.organizacaorecursos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProfessorCreateRequest(
        @NotBlank String nome,
        @NotBlank @Email String email,
        @NotBlank String competencia,
        @NotNull Boolean ativo) { }
