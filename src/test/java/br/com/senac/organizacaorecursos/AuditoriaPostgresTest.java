package br.com.senac.organizacaorecursos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import br.com.senac.organizacaorecursos.entity.Material;
import br.com.senac.organizacaorecursos.entity.Perfil;
import br.com.senac.organizacaorecursos.entity.Reserva;
import br.com.senac.organizacaorecursos.entity.StatusReserva;
import br.com.senac.organizacaorecursos.entity.Usuario;
import br.com.senac.organizacaorecursos.repository.AuditoriaRepository;
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
class AuditoriaPostgresTest {
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
    @Autowired private ReservaRepository reservaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private MaterialRepository materialRepository;
    @Autowired private AuditoriaRepository auditoriaRepository;
    @Autowired private NotificacaoRepository notificacaoRepository;
    @Autowired private ObjectMapper objectMapper;

    private Usuario solicitante;
    private Usuario responsavel;
    private Material material;

    @BeforeEach
    void preparaBanco() {
        notificacaoRepository.deleteAll();
        auditoriaRepository.deleteAll();
        reservaRepository.deleteAll();
        materialRepository.deleteAll();
        usuarioRepository.deleteAll();

        solicitante = salvarUsuario("ana@senac.br", Perfil.SOLICITANTE);
        responsavel = salvarUsuario("responsavel@senac.br", Perfil.RESPONSAVEL);
        material = materialRepository.saveAndFlush(new Material("Projetor", "Equipamento", 2, 2, "Almoxarifado", false, true));
    }

    @Test
    void registraEventoDeRetiradaComUsuarioETimestampCorretos() throws Exception {
        Reserva reserva = reservaRepository.save(new Reserva(solicitante, null, null, material,
                OffsetDateTime.now().plusHours(1), OffsetDateTime.now().plusHours(2), StatusReserva.APROVADA));

        mockMvc.perform(post("/api/reservas/{id}/retirada", reserva.getId())
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isOk());

        var auditorias = auditoriaRepository.findAllByOrderByCriadoEmDesc();
        assertThat(auditorias).anySatisfy(auditoria -> {
            assertThat(auditoria.getTipoEntidade()).isEqualTo("RESERVA");
            assertThat(auditoria.getAcao()).isEqualTo("RETIRADA");
            assertThat(auditoria.getUsuario().getEmail()).isEqualTo(responsavel.getEmail());
            assertThat(auditoria.getCriadoEm()).isNotNull();
            assertThat(auditoria.getEstadoAnterior()).isEqualTo("APROVADA");
            assertThat(auditoria.getEstadoPosterior()).isEqualTo("EM_USO");
        });
    }

    @Test
    void consultaAuditoriaRequerPerfilResponsavelOuAdmin() throws Exception {
        mockMvc.perform(get("/api/auditorias")
                        .header("Authorization", token(solicitante)))
                .andExpect(status().isForbidden());
    }

    @Test
    void operacaoInvalidaNaoGeraAuditoriaDeSucesso() throws Exception {
        Reserva reserva = reservaRepository.save(new Reserva(solicitante, null, null, material,
                OffsetDateTime.now().plusHours(1), OffsetDateTime.now().plusHours(2), StatusReserva.APROVADA));

        mockMvc.perform(post("/api/reservas/{id}/retirada", reserva.getId())
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/reservas/{id}/retirada", reserva.getId())
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isConflict());

        assertThat(auditoriaRepository.findAllByOrderByCriadoEmDesc()).filteredOn(auditoria -> auditoria.getAcao().equals("RETIRADA")).hasSize(1);
    }

    @Test
    void registraAlteracaoDeReservaNoHistorico() throws Exception {
        Reserva reserva = reservaRepository.saveAndFlush(new Reserva(solicitante, null, null, material,
                OffsetDateTime.now().plusHours(1), OffsetDateTime.now().plusHours(2), StatusReserva.APROVADA));

        mockMvc.perform(put("/api/reservas/{id}", reserva.getId())
                        .header("Authorization", token(solicitante))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"materialId\":" + material.getId()
                                + ",\"inicio\":\"2026-11-04T11:00:00-03:00\""
                                + ",\"termino\":\"2026-11-04T12:00:00-03:00\"}"))
                .andExpect(status().isOk());

        assertThat(auditoriaRepository.findAllByOrderByCriadoEmDesc()).anySatisfy(auditoria -> {
            assertThat(auditoria.getAcao()).isEqualTo("ALTERADA");
            assertThat(auditoria.getEstadoAnterior()).isEqualTo("APROVADA");
            assertThat(auditoria.getEstadoPosterior()).isEqualTo("APROVADA");
            try {
                JsonNode detalhes = objectMapper.readTree(auditoria.getDetalhes());
                assertThat(detalhes.path("operacao").asText()).isEqualTo("ALTERADA");
                assertThat(detalhes.path("antes").path("recurso").path("id").asLong())
                        .isEqualTo(material.getId());
                assertThat(OffsetDateTime.parse(detalhes.path("depois").path("inicio").asText()).toInstant())
                        .isEqualTo(OffsetDateTime.parse("2026-11-04T11:00:00-03:00").toInstant());
                assertThat(auditoria.getDetalhes()).doesNotContain("senha", "token", "jwt");
            } catch (Exception exception) {
                throw new AssertionError("Auditoria estruturada invalida", exception);
            }
        });
    }

    private Usuario salvarUsuario(String email, Perfil perfil) {
        return usuarioRepository.save(new Usuario("Usuario", email, BCRYPT_HASH, perfil, true));
    }

    private String token(Usuario usuario) {
        return "Bearer " + jwtService.generateToken(usuario);
    }
}
