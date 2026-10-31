package br.com.senac.organizacaorecursos.service;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() { super("Credenciais inválidas."); }
}
