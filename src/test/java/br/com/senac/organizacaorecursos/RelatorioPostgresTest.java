package br.com.senac.organizacaorecursos;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.senac.organizacaorecursos.entity.Perfil;
import br.com.senac.organizacaorecursos.entity.Professor;
import br.com.senac.organizacaorecursos.entity.Reserva;
import br.com.senac.organizacaorecursos.entity.Sala;
import br.com.senac.organizacaorecursos.entity.StatusReserva;
import br.com.senac.organizacaorecursos.entity.Usuario;
import br.com.senac.organizacaorecursos.repository.AuditoriaRepository;
import br.com.senac.organizacaorecursos.repository.ManutencaoRepository;
import br.com.senac.organizacaorecursos.repository.NotificacaoRepository;
import br.com.senac.organizacaorecursos.repository.ProfessorRepository;
import br.com.senac.organizacaorecursos.repository.ReservaRepository;
import br.com.senac.organizacaorecursos.repository.SalaRepository;
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
class RelatorioPostgresTest {
    private static final String BCRYPT_HASH = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
    private static final String INICIO = "2026-11-04T09:00:00-03:00";
    private static final String TERMINO = "2026-11-04T12:00:00-03:00";

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
    @Autowired private SalaRepository salaRepository;
    @Autowired private ProfessorRepository professorRepository;
    @Autowired private ReservaRepository reservaRepository;
    @Autowired private ManutencaoRepository manutencaoRepository;
    @Autowired private AuditoriaRepository auditoriaRepository;
    @Autowired private NotificacaoRepository notificacaoRepository;

    private Usuario solicitante;
    private Usuario responsavel;
    private Sala sala;
    private Professor professor;

    @BeforeEach
    void preparaBanco() {
        notificacaoRepository.deleteAll();
        auditoriaRepository.deleteAll();
        reservaRepository.deleteAll();
        manutencaoRepository.deleteAll();
        professorRepository.deleteAll();
        salaRepository.deleteAll();
        usuarioRepository.deleteAll();

        solicitante = salvarUsuario("ana@senac.br", Perfil.SOLICITANTE);
        responsavel = salvarUsuario("responsavel@senac.br", Perfil.RESPONSAVEL);
        sala = salaRepository.saveAndFlush(new Sala("Sala 1", "Aula", 30, "Bloco A", false, true));
        professor = professorRepository.saveAndFlush(
                new Professor("Carlos Souza", "carlos@senac.br", "Matematica", true));
    }

    @Test
    void geraUtilizacaoECargaHorariaComDadosPersistidos() throws Exception {
        reservaRepository.saveAndFlush(new Reserva(solicitante, sala, null, null,
                data("2026-11-04T09:00:00-03:00"), data("2026-11-04T11:00:00-03:00"),
                StatusReserva.CONCLUIDA));
        reservaRepository.saveAndFlush(new Reserva(solicitante, null, professor, null,
                data("2026-11-04T10:00:00-03:00"), data("2026-11-04T11:00:00-03:00"),
                StatusReserva.EM_USO));

        mockMvc.perform(get("/api/relatorios/utilizacao").param("inicio", INICIO).param("termino", TERMINO)
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReservas").value(2))
                .andExpect(jsonPath("$.totalHorasUtilizadas").value(3.0))
                .andExpect(jsonPath("$.porTipoRecurso.length()").value(2));

        mockMvc.perform(get("/api/relatorios/carga-horaria").param("inicio", INICIO).param("termino", TERMINO)
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalHorasReservadas").value(3.0))
                .andExpect(jsonPath("$.porSolicitante[0].solicitanteEmail").value(solicitante.getEmail()))
                .andExpect(jsonPath("$.porSolicitante[0].horasReservadas").value(3.0));
    }

    @Test
    void periodoSemDadosRetornaColecoesVaziasEZero() throws Exception {
        mockMvc.perform(get("/api/relatorios/utilizacao")
                        .param("inicio", "2026-11-05T09:00:00-03:00")
                        .param("termino", "2026-11-05T12:00:00-03:00")
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReservas").value(0))
                .andExpect(jsonPath("$.porTipoRecurso.length()").value(0));

        mockMvc.perform(get("/api/relatorios/carga-horaria")
                        .param("inicio", "2026-11-05T09:00:00-03:00")
                        .param("termino", "2026-11-05T12:00:00-03:00")
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalHorasReservadas").value(0))
                .andExpect(jsonPath("$.porSolicitante.length()").value(0));
    }

    @Test
    void registraEConsultaConflitoEvitadoPersistido() throws Exception {
        reservaRepository.saveAndFlush(new Reserva(solicitante, sala, null, null,
                data(INICIO), data("2026-11-04T10:30:00-03:00"), StatusReserva.APROVADA));

        mockMvc.perform(post("/api/reservas").header("Authorization", token(solicitante))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"salaId\":" + sala.getId()
                                + ",\"inicio\":\"2026-11-04T10:00:00-03:00\","
                                + "\"termino\":\"2026-11-04T11:00:00-03:00\"}"))
                .andExpect(status().isConflict());

        var auditoriaConflito = auditoriaRepository.findAllByOrderByCriadoEmDesc().stream()
                .filter(auditoria -> "CONFLITO_EVITADO".equals(auditoria.getAcao()))
                .findFirst().orElseThrow();
        mockMvc.perform(get("/api/relatorios/conflitos-evitados")
                        .param("inicio", auditoriaConflito.getCriadoEm().minusSeconds(1).toString())
                        .param("termino", auditoriaConflito.getCriadoEm().plusSeconds(1).toString())
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalConflitosEvitados").value(1));
    }

    @Test
    void exigePerfilResponsavelOuAdminParaRelatorios() throws Exception {
        mockMvc.perform(get("/api/relatorios/utilizacao").param("inicio", INICIO).param("termino", TERMINO))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/relatorios/utilizacao").param("inicio", INICIO).param("termino", TERMINO)
                        .header("Authorization", token(solicitante)))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejeitaPeriodoInvertidoComContratoDeErro() throws Exception {
        mockMvc.perform(get("/api/relatorios/utilizacao")
                        .param("inicio", TERMINO).param("termino", INICIO)
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("O periodo deve ter termino posterior ao inicio."))
                .andExpect(jsonPath("$.path").value("/api/relatorios/utilizacao"));
    }

    private Usuario salvarUsuario(String email, Perfil perfil) {
        return usuarioRepository.saveAndFlush(new Usuario("Usuario", email, BCRYPT_HASH, perfil, true));
    }

    private OffsetDateTime data(String valor) {
        return OffsetDateTime.parse(valor);
    }

    private String token(Usuario usuario) {
        return "Bearer " + jwtService.generateToken(usuario);
    }
}
