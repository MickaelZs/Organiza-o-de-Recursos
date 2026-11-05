package br.com.senac.organizacaorecursos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.senac.organizacaorecursos.entity.Material;
import br.com.senac.organizacaorecursos.entity.Perfil;
import br.com.senac.organizacaorecursos.entity.Reserva;
import br.com.senac.organizacaorecursos.entity.Usuario;
import br.com.senac.organizacaorecursos.repository.MaterialRepository;
import br.com.senac.organizacaorecursos.repository.NotificacaoRepository;
import br.com.senac.organizacaorecursos.repository.ReservaRepository;
import br.com.senac.organizacaorecursos.repository.UsuarioRepository;
import br.com.senac.organizacaorecursos.security.JwtService;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("postgres-test")
@Testcontainers
class NotificacaoPostgresTest {
    private static final String BCRYPT_HASH = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    @Container
    static final PostgreSQLContainer<?> postgresql = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("organizacao_recursos_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresql::getJdbcUrl);
        registry.add("spring.datasource.username", postgresql::getUsername);
        registry.add("spring.datasource.password", postgresql::getPassword);
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private MaterialRepository materialRepository;
    @Autowired private ReservaRepository reservaRepository;
    @Autowired private NotificacaoRepository notificacaoRepository;
    @Autowired private br.com.senac.organizacaorecursos.repository.AuditoriaRepository auditoriaRepository;

    private Usuario solicitante;
    private Usuario outroSolicitante;
    private Usuario responsavel;
    private Material materialRestrito;

    @BeforeEach
    void preparaBanco() {
        notificacaoRepository.deleteAll();
        auditoriaRepository.deleteAll();
        reservaRepository.deleteAll();
        materialRepository.deleteAll();
        usuarioRepository.deleteAll();

        solicitante = salvarUsuario("ana@senac.br", Perfil.SOLICITANTE);
        outroSolicitante = salvarUsuario("bia@senac.br", Perfil.SOLICITANTE);
        responsavel = salvarUsuario("responsavel@senac.br", Perfil.RESPONSAVEL);
        materialRestrito = materialRepository.saveAndFlush(new Material("Camera restrita", "Equipamento", 1, 1,
                "Almoxarifado", true, true));
    }

    @Test
    void aprovacaoDeReservaGeraNotificacaoParaSolicitante() throws Exception {
        String payload = "{\"materialId\":" + materialRestrito.getId() + ",\"inicio\":\"2026-11-03T09:00:00-03:00\",\"termino\":\"2026-11-03T10:00:00-03:00\"}";
        mockMvc.perform(post("/api/reservas")
                        .header("Authorization", token(solicitante))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        Reserva reserva = reservaRepository.findAll().getFirst();
        mockMvc.perform(post("/api/reservas/{id}/aprovar", reserva.getId())
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isOk());

        assertThat(notificacaoRepository.findAllByOrderByCriadaEmDesc()).anySatisfy(notificacao -> {
            assertThat(notificacao.getDestinatario().getEmail()).isEqualTo(solicitante.getEmail());
            assertThat(notificacao.getTipo()).isEqualTo("RESERVA_APROVADA");
            assertThat(notificacao.getMensagem()).contains("aprovada");
            assertThat(notificacao.getEntidadeTipo()).isEqualTo("RESERVA");
        });
    }

    @Test
    void consultaDeNotificacoesEhLimitadaAoUsuarioLogado() throws Exception {
        String payload = "{\"materialId\":" + materialRestrito.getId() + ",\"inicio\":\"2026-11-03T09:00:00-03:00\",\"termino\":\"2026-11-03T10:00:00-03:00\"}";
        mockMvc.perform(post("/api/reservas")
                        .header("Authorization", token(solicitante))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        Reserva reserva = reservaRepository.findAll().getFirst();
        mockMvc.perform(post("/api/reservas/{id}/aprovar", reserva.getId())
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notificacoes").header("Authorization", token(solicitante)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("RESERVA_APROVADA"));

        mockMvc.perform(get("/api/notificacoes").header("Authorization", token(outroSolicitante)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", org.hamcrest.Matchers.is(0)));
    }

    @Test
    void marcarNotificacaoComoLidaPersisteEstado() throws Exception {
        String payload = "{\"materialId\":" + materialRestrito.getId() + ",\"inicio\":\"2026-11-03T09:00:00-03:00\",\"termino\":\"2026-11-03T10:00:00-03:00\"}";
        mockMvc.perform(post("/api/reservas")
                        .header("Authorization", token(solicitante))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        Reserva reserva = reservaRepository.findAll().getFirst();
        mockMvc.perform(post("/api/reservas/{id}/aprovar", reserva.getId())
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isOk());

        Long id = notificacaoRepository.findAllByOrderByCriadaEmDesc().getFirst().getId();
        mockMvc.perform(patch("/api/notificacoes/{id}/lida", id)
                        .header("Authorization", token(solicitante)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lida").value(true));

        assertThat(notificacaoRepository.findById(id).orElseThrow().getLida()).isTrue();
    }

    private Usuario salvarUsuario(String email, Perfil perfil) {
        return usuarioRepository.save(new Usuario("Usuario", email, BCRYPT_HASH, perfil, true));
    }

    private String token(Usuario usuario) {
        return "Bearer " + jwtService.generateToken(usuario);
    }
}
