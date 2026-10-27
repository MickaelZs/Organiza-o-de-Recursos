package br.com.senac.organizacaorecursos.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.senac.organizacaorecursos.config.JwtProperties;
import br.com.senac.organizacaorecursos.entity.Perfil;
import br.com.senac.organizacaorecursos.entity.Usuario;
import io.jsonwebtoken.JwtException;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
    private static final String SECRET = "dGVzdC1qd3Qtc2VjcmV0LXdpdGgtYXQtbGVhc3QtMzItYnl0ZXMtbG9uZw==";

    @Test
    void aceitaTokenValidoERecuperaPerfil() {
        JwtService jwtService = service(Duration.ofHours(1));
        String token = jwtService.generateToken(new Usuario("Ana", "ana@senac.br", "hash", Perfil.ADMIN, true));

        assertThat(jwtService.getClaims(token).getSubject()).isEqualTo("ana@senac.br");
        assertThat(jwtService.getClaims(token).get("perfil", String.class)).isEqualTo("ADMIN");
    }

    @Test
    void rejeitaTokenInvalido() {
        assertThatThrownBy(() -> service(Duration.ofHours(1)).getClaims("nao-e-um-token"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void rejeitaTokenExpirado() {
        JwtService expiredService = service(Duration.ofSeconds(-1));
        String token = expiredService.generateToken(new Usuario("Ana", "ana@senac.br", "hash", Perfil.ADMIN, true));

        assertThatThrownBy(() -> expiredService.getClaims(token)).isInstanceOf(JwtException.class);
    }

    private JwtService service(Duration expiration) {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(SECRET);
        properties.setExpiration(expiration);
        return new JwtService(properties);
    }
}
