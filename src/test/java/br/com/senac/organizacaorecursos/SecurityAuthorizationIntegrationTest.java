package br.com.senac.organizacaorecursos;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.senac.organizacaorecursos.config.JwtProperties;
import br.com.senac.organizacaorecursos.entity.Perfil;
import br.com.senac.organizacaorecursos.entity.Usuario;
import br.com.senac.organizacaorecursos.repository.UsuarioRepository;
import br.com.senac.organizacaorecursos.security.JwtService;
import java.time.Duration;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAuthorizationIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @ParameterizedTest(name = "{0}")
    @MethodSource("operacoesPublicas")
    void permiteOperacoesPublicasSemToken(String descricao, String method, String path, String payload) throws Exception {
        if ("GET".equals(method)) {
            mockMvc.perform(get(path)).andExpect(status().isOk());
        } else {
            mockMvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(payload))
                    .andExpect(status().isBadRequest());
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("credenciaisInvalidas")
    void rejeitaCredenciaisInvalidasEmRotaProtegida(String descricao, String authorization) throws Exception {
        mockMvc.perform(get("/api/inexistente").header("Authorization", authorization))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "token valido de {0} atravessa a autenticacao antes de chegar ao mapeamento")
    @MethodSource("perfis")
    void aceitaTokenValidoDeCadaPerfilNaCamadaDeAutenticacao(Perfil perfil) throws Exception {
        String token = jwtService.generateToken(new Usuario("Ana", "ana@senac.br", "hash", perfil, true));

        mockMvc.perform(get("/api/inexistente").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    private static Stream<Arguments> operacoesPublicas() {
        return Stream.of(
                Arguments.of("health sem token", "GET", "/api/health", ""),
                Arguments.of("login sem token", "POST", "/api/auth/login", "{}"));
    }

    private Stream<Arguments> credenciaisInvalidas() {
        JwtProperties expiredProperties = new JwtProperties();
        expiredProperties.setSecret("dGVzdC1qd3Qtc2VjcmV0LXdpdGgtYXQtbGVhc3QtMzItYnl0ZXMtbG9uZw==");
        expiredProperties.setExpiration(Duration.ofSeconds(-1));
        String expiredToken = new JwtService(expiredProperties).generateToken(
                new Usuario("Ana", "ana@senac.br", "hash", Perfil.ADMIN, true));
        return Stream.of(
                Arguments.of("token ausente", null),
                Arguments.of("token malformado", "Bearer nao-e-um-token"),
                Arguments.of("token expirado", "Bearer " + expiredToken));
    }

    private static Stream<Perfil> perfis() {
        return Stream.of(Perfil.SOLICITANTE, Perfil.RESPONSAVEL, Perfil.ADMIN);
    }
}
