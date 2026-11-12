package br.com.senac.organizacaorecursos.cli;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class AdminBootstrapCommandTest {
    @Test
    void aceitaSomenteUrlPostgresLocalComPortaEDatabaseExplicitos() {
        assertThatCode(() -> AdminBootstrapCommand.validarUrlLocal(
                "jdbc:postgresql://localhost:5432/organizacao_recursos"))
                .doesNotThrowAnyException();
        assertThatCode(() -> AdminBootstrapCommand.validarUrlLocal(
                "jdbc:postgresql://127.0.0.1:5432/organizacao_recursos"))
                .doesNotThrowAnyException();
    }

    @Test
    void rejeitaUrlRemotaSemPortaCredenciaisOuParametros() {
        assertThatThrownBy(() -> AdminBootstrapCommand.validarUrlLocal(
                "jdbc:postgresql://database.example.com:5432/organizacao_recursos"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AdminBootstrapCommand.validarUrlLocal(
                "jdbc:postgresql://localhost/organizacao_recursos"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AdminBootstrapCommand.validarUrlLocal(
                "jdbc:postgresql://user:password@localhost:5432/organizacao_recursos"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AdminBootstrapCommand.validarUrlLocal(
                "jdbc:postgresql://localhost:5432/organizacao_recursos?currentSchema=public"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
