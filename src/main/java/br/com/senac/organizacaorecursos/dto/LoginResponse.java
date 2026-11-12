package br.com.senac.organizacaorecursos.dto;

public record LoginResponse(String token, String tipo) {
    @Override
    public String toString() {
        return "LoginResponse[token=[REDACTED], tipo=" + tipo + "]";
    }
}
