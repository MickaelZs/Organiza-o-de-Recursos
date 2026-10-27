package br.com.senac.organizacaorecursos.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class PasswordEncoderTest {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void geraHashDiferenteDaSenhaEValidaSenhaCorreta() {
        String hash = encoder.encode("senha-original");

        assertThat(hash).isNotEqualTo("senha-original");
        assertThat(encoder.matches("senha-original", hash)).isTrue();
    }

    @Test
    void rejeitaSenhaIncorreta() {
        assertThat(encoder.matches("senha-incorreta", encoder.encode("senha-original"))).isFalse();
    }
}
