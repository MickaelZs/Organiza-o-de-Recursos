package br.com.senac.organizacaorecursos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.senac.organizacaorecursos.entity.Material;
import br.com.senac.organizacaorecursos.entity.Perfil;
import br.com.senac.organizacaorecursos.entity.Reserva;
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
class ReservaRetiradaDevolucaoPostgresTest {
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
    @Autowired private NotificacaoRepository notificacaoRepository;
    @Autowired private AuditoriaRepository auditoriaRepository;

    private Usuario solicitante;
    private Usuario responsavel;
    private Usuario admin;
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
        admin = salvarUsuario("admin@senac.br", Perfil.ADMIN);
        material = materialRepository.saveAndFlush(new Material("Projetor", "Equipamento", 2, 2, "Almoxarifado", false, true));
    }

    @Test
    void retiradaValidaPorResponsavelAtualizaStatusERegistro() throws Exception {
        Long reservaId = criarReservaAprovada();

        mockMvc.perform(post("/api/reservas/{id}/retirada", reservaId)
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_USO"))
                .andExpect(jsonPath("$.retiradoEm").exists());

        Reserva reserva = reservaRepository.findById(reservaId).orElseThrow();
        assertThat(reserva.getRetiradoEm()).isNotNull();
        assertThat(reserva.getStatus()).isEqualTo(br.com.senac.organizacaorecursos.entity.StatusReserva.EM_USO);
    }

    @Test
    void devolucaoValidaDepoisDaRetiradaAtualizaStatus() throws Exception {
        Long reservaId = criarReservaAprovada();

        mockMvc.perform(post("/api/reservas/{id}/retirada", reservaId)
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/reservas/{id}/devolucao", reservaId)
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDA"))
                .andExpect(jsonPath("$.devolvidoEm").exists());

        Reserva reserva = reservaRepository.findById(reservaId).orElseThrow();
        assertThat(reserva.getDevolvidoEm()).isNotNull();
    }

    @Test
    void retiradaDuplicadaRetornaConflito() throws Exception {
        Long reservaId = criarReservaAprovada();

        mockMvc.perform(post("/api/reservas/{id}/retirada", reservaId)
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/reservas/{id}/retirada", reservaId)
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isConflict());
    }

    @Test
    void devolucaoSemRetiradaRetornaConflito() throws Exception {
        Long reservaId = criarReservaAprovada();

        mockMvc.perform(post("/api/reservas/{id}/devolucao", reservaId)
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isConflict());
    }

    @Test
    void devolucaoDuplicadaRetornaConflito() throws Exception {
        Long reservaId = criarReservaAprovada();

        mockMvc.perform(post("/api/reservas/{id}/retirada", reservaId)
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/reservas/{id}/devolucao", reservaId)
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/reservas/{id}/devolucao", reservaId)
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isConflict());
    }

    @Test
    void retiradaDeReservaCanceladaRetornaConflito() throws Exception {
        Reserva reserva = reservaRepository.save(new Reserva(solicitante, null, null, material,
                OffsetDateTime.now().plusHours(1), OffsetDateTime.now().plusHours(2),
                br.com.senac.organizacaorecursos.entity.StatusReserva.CANCELADA));

        mockMvc.perform(post("/api/reservas/{id}/retirada", reserva.getId())
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isConflict());
    }

    @Test
    void retiradaSemAutorizacaoRetornaForbidden() throws Exception {
        Long reservaId = criarReservaAprovada();

        mockMvc.perform(post("/api/reservas/{id}/retirada", reservaId)
                        .header("Authorization", token(solicitante)))
                .andExpect(status().isForbidden());
    }

    @Test
    void retiradaEDevolucaoPersistemTimestamps() throws Exception {
        Long reservaId = criarReservaAprovada();

        mockMvc.perform(post("/api/reservas/{id}/retirada", reservaId)
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/reservas/{id}/devolucao", reservaId)
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isOk());

        Reserva reserva = reservaRepository.findById(reservaId).orElseThrow();
        assertThat(reserva.getRetiradoEm()).isNotNull();
        assertThat(reserva.getDevolvidoEm()).isNotNull();
    }

    @Test
    void retiradaEmReservaInexistenteRetornaNotFound() throws Exception {
        mockMvc.perform(post("/api/reservas/{id}/retirada", 999_999L)
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isNotFound());
    }

    private Long criarReservaAprovada() {
        Reserva reserva = reservaRepository.save(new Reserva(solicitante, null, null, material,
                OffsetDateTime.now().plusHours(1), OffsetDateTime.now().plusHours(2),
                br.com.senac.organizacaorecursos.entity.StatusReserva.APROVADA));
        return reserva.getId();
    }

    private Usuario salvarUsuario(String email, Perfil perfil) {
        return usuarioRepository.save(new Usuario("Usuario", email, BCRYPT_HASH, perfil, true));
    }

    private String token(Usuario usuario) {
        return "Bearer " + jwtService.generateToken(usuario);
    }
}
