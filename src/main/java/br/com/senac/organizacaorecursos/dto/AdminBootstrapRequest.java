package br.com.senac.organizacaorecursos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminBootstrapRequest(
        @NotBlank @Size(max = 255) String nome,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 12) CharSequence senha) {
    @Override
    public String toString() {
        return "AdminBootstrapRequest[nome=" + nome + ", email=" + email + ", senha=[REDACTED]]";
    }
}
