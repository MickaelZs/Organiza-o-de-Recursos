package br.com.senac.organizacaorecursos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import br.com.senac.organizacaorecursos.entity.Perfil;
import br.com.senac.organizacaorecursos.entity.Usuario;
import br.com.senac.organizacaorecursos.repository.UsuarioRepository;
import br.com.senac.organizacaorecursos.security.JwtService;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class AuthServiceTest {
    @Test
    void autenticaUsuarioAtivoComSenhaCorreta() {
        UsuarioRepository repository = Mockito.mock(UsuarioRepository.class);
        JwtService jwtService = Mockito.mock(JwtService.class);
        Usuario usuario = new Usuario("Ana", "ana@senac.br", new BCryptPasswordEncoder().encode("senha"), Perfil.ADMIN, true);
        when(repository.findByEmail("ana@senac.br")).thenReturn(Optional.of(usuario));
        when(jwtService.generateToken(usuario)).thenReturn("jwt-valido");

        String token = new AuthService(repository, new BCryptPasswordEncoder(), jwtService).login("ana@senac.br", "senha");

        assertThat(token).isEqualTo("jwt-valido");
    }

    @ParameterizedTest(name = "rejeita credenciais quando {0}")
    @MethodSource("credenciaisInvalidas")
    void rejeitaCredenciaisInvalidas(String descricao, Optional<Usuario> usuario, String senha) {
        UsuarioRepository repository = Mockito.mock(UsuarioRepository.class);
        when(repository.findByEmail("ana@senac.br")).thenReturn(usuario);

        AuthService service = new AuthService(repository, new BCryptPasswordEncoder(), Mockito.mock(JwtService.class));

        assertThatThrownBy(() -> service.login("ana@senac.br", senha)).isInstanceOf(InvalidCredentialsException.class);
    }

    private static Stream<Arguments> credenciaisInvalidas() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        Usuario usuario = new Usuario("Ana", "ana@senac.br", encoder.encode("senha-correta"), Perfil.SOLICITANTE, true);
        return Stream.of(
                Arguments.of("a senha está incorreta", Optional.of(usuario), "senha-errada"),
                Arguments.of("o usuário não existe", Optional.empty(), "qualquer-senha"));
    }
}
