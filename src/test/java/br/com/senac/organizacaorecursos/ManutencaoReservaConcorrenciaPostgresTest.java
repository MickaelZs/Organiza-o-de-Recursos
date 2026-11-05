package br.com.senac.organizacaorecursos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.senac.organizacaorecursos.dto.ManutencaoRequest;
import br.com.senac.organizacaorecursos.dto.ReservaRequest;
import br.com.senac.organizacaorecursos.entity.Material;
import br.com.senac.organizacaorecursos.entity.Manutencao;
import br.com.senac.organizacaorecursos.entity.Perfil;
import br.com.senac.organizacaorecursos.entity.Professor;
import br.com.senac.organizacaorecursos.entity.Sala;
import br.com.senac.organizacaorecursos.entity.StatusManutencao;
import br.com.senac.organizacaorecursos.entity.Usuario;
import br.com.senac.organizacaorecursos.repository.AuditoriaRepository;
import br.com.senac.organizacaorecursos.repository.MaterialRepository;
import br.com.senac.organizacaorecursos.repository.ManutencaoRepository;
import br.com.senac.organizacaorecursos.repository.NotificacaoRepository;
import br.com.senac.organizacaorecursos.repository.ProfessorRepository;
import br.com.senac.organizacaorecursos.repository.ReservaRepository;
import br.com.senac.organizacaorecursos.repository.SalaRepository;
import br.com.senac.organizacaorecursos.repository.UsuarioRepository;
import br.com.senac.organizacaorecursos.security.JwtService;
import br.com.senac.organizacaorecursos.service.ManutencaoService;
import br.com.senac.organizacaorecursos.service.ReservaService;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
class ManutencaoReservaConcorrenciaPostgresTest {
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
    @Autowired private ReservaService reservaService;
    @Autowired private ManutencaoService manutencaoService;
    @Autowired private SalaRepository salaRepository;
    @Autowired private ProfessorRepository professorRepository;
    @Autowired private MaterialRepository materialRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ReservaRepository reservaRepository;
    @Autowired private ManutencaoRepository manutencaoRepository;
    @Autowired private NotificacaoRepository notificacaoRepository;
    @Autowired private AuditoriaRepository auditoriaRepository;

    private Usuario solicitante;
    private Usuario responsavel;
    private Sala sala;

    @BeforeEach
    void prepararDados() {
        notificacaoRepository.deleteAll();
        auditoriaRepository.deleteAll();
        reservaRepository.deleteAll();
        manutencaoRepository.deleteAll();
        materialRepository.deleteAll();
        professorRepository.deleteAll();
        salaRepository.deleteAll();
        usuarioRepository.deleteAll();

        solicitante = usuarioRepository.saveAndFlush(new Usuario("Solicitante", "solicitante@senac.br", BCRYPT_HASH, Perfil.SOLICITANTE, true));
        responsavel = usuarioRepository.saveAndFlush(new Usuario("Responsavel", "responsavel@senac.br", BCRYPT_HASH, Perfil.RESPONSAVEL, true));
        sala = salaRepository.saveAndFlush(new Sala("Sala concorrencia", "Aula", 40, "Bloco A", false, true));
    }

    @Test
    void reservaEManutencaoDisputamMesmoRecursoSemSobreposicao() throws Exception {
        for (int i = 0; i < 3; i++) {
            prepararDados();
            OffsetDateTime inicio = OffsetDateTime.parse("2026-11-03T10:00:00-03:00");
            OffsetDateTime termino = OffsetDateTime.parse("2026-11-03T12:00:00-03:00");

            CountDownLatch start = new CountDownLatch(1);
            ExecutorService executor = Executors.newFixedThreadPool(2);
            List<Future<OperationResult>> futures = new ArrayList<>();
            futures.add(executor.submit(criarReservaEmParalelo(start, sala, solicitante, inicio, termino)));
            futures.add(executor.submit(criarManutencaoEmParalelo(start, sala, responsavel, inicio, termino)));
            start.countDown();

            int successes = 0;
            int conflicts = 0;
            for (Future<OperationResult> future : futures) {
                OperationResult result = future.get();
                if (result.success()) {
                    successes++;
                } else {
                    conflicts++;
                }
            }
            executor.shutdownNow();

            assertThat(successes).isEqualTo(1);
            assertThat(conflicts).isEqualTo(1);

            int reservasAtivas = reservaRepository.findAll().size();
            int manutencoesAtivas = manutencaoRepository.findAll().size();
            assertThat(reservasAtivas + manutencoesAtivas).isEqualTo(1);
            assertThat(reservasAtivas == 0 || manutencoesAtivas == 0).isTrue();
            assertThat(reservaRepository.findAll()).allSatisfy(reserva -> assertThat(reserva.getStatus()).isNotNull());
            assertThat(manutencaoRepository.findAll()).allSatisfy(manutencao -> assertThat(manutencao.getStatus()).isEqualTo(StatusManutencao.ATIVA));
        }
    }

    @Test
    void manutencaoAtivaBloqueiaDisponibilidadeEAdjacenciaPermaneceLivre() throws Exception {
        OffsetDateTime manutencaoInicio = OffsetDateTime.parse("2026-11-03T10:00:00-03:00");
        OffsetDateTime manutencaoTermino = OffsetDateTime.parse("2026-11-03T12:00:00-03:00");
        manutencaoRepository.saveAndFlush(new Manutencao(
                br.com.senac.organizacaorecursos.entity.TipoRecurso.SALA,
                sala,
                null,
                null,
                manutencaoInicio,
                manutencaoTermino,
                "Manutencao preventiva",
                responsavel,
                OffsetDateTime.now(),
                StatusManutencao.ATIVA));

        mockMvc.perform(get("/api/salas")
                        .param("inicio", "2026-11-03T11:00:00-03:00")
                        .param("termino", "2026-11-03T11:30:00-03:00")
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getContentAsString()).doesNotContain("\"id\":" + sala.getId()));

        mockMvc.perform(get("/api/salas")
                        .param("inicio", "2026-11-03T12:00:00-03:00")
                        .param("termino", "2026-11-03T13:00:00-03:00")
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getContentAsString()).contains("\"id\":" + sala.getId()));
    }

    @Test
    void manutencoesAtivasSobrepostasParaMesmoRecursoSaoRejeitadas() {
        OffsetDateTime inicio = OffsetDateTime.parse("2026-11-03T10:00:00-03:00");
        OffsetDateTime termino = OffsetDateTime.parse("2026-11-03T12:00:00-03:00");
        manutencaoService.criar(responsavel.getEmail(), new ManutencaoRequest(sala.getId(), null, null, inicio, termino, "Primeira manutencao"));

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () ->
                manutencaoService.criar(responsavel.getEmail(), new ManutencaoRequest(sala.getId(), null, null, inicio.plusHours(1), termino.plusHours(1), "Segunda manutencao")));
    }

    @Test
    void cancelarManutencaoLiberaRecursoParaNovaReserva() throws Exception {
        OffsetDateTime inicio = OffsetDateTime.parse("2026-11-03T10:00:00-03:00");
        OffsetDateTime termino = OffsetDateTime.parse("2026-11-03T12:00:00-03:00");
        Manutencao manutencao = manutencaoRepository.saveAndFlush(new Manutencao(
                br.com.senac.organizacaorecursos.entity.TipoRecurso.SALA,
                sala,
                null,
                null,
                inicio,
                termino,
                "Manutencao preventiva",
                responsavel,
                OffsetDateTime.now(),
                StatusManutencao.ATIVA));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/manutencoes/" + manutencao.getId())
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isNoContent());

        Manutencao cancelada = manutencaoRepository.findById(manutencao.getId()).orElseThrow();
        assertThat(cancelada.getStatus()).isEqualTo(StatusManutencao.CANCELADA);

        mockMvc.perform(post("/api/reservas")
                        .header("Authorization", token(solicitante))
                        .contentType("application/json")
                        .content("{\"salaId\":" + sala.getId() + ",\"inicio\":\"2026-11-03T10:30:00-03:00\",\"termino\":\"2026-11-03T11:30:00-03:00\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/manutencoes")
                        .header("Authorization", token(responsavel))
                        .contentType("application/json")
                        .content("{\"salaId\":" + sala.getId() + ",\"inicio\":\"2026-11-03T10:30:00-03:00\",\"termino\":\"2026-11-03T11:30:00-03:00\",\"motivo\":\"Nova manutencao\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/manutencoes/" + manutencao.getId())
                        .header("Authorization", token(solicitante)))
                .andExpect(status().isForbidden());
    }

    private Callable<OperationResult> criarReservaEmParalelo(CountDownLatch start, Sala sala, Usuario solicitante,
                                                            OffsetDateTime inicio, OffsetDateTime termino) {
        return () -> {
            start.await();
            try {
                reservaService.criar(solicitante.getEmail(), new ReservaRequest(sala.getId(), null, null, inicio, termino));
                return new OperationResult("reserva", true, null);
            } catch (RuntimeException ex) {
                return new OperationResult("reserva", false, ex.getClass().getSimpleName());
            }
        };
    }

    private Callable<OperationResult> criarManutencaoEmParalelo(CountDownLatch start, Sala sala, Usuario responsavel,
                                                               OffsetDateTime inicio, OffsetDateTime termino) {
        return () -> {
            start.await();
            try {
                manutencaoService.criar(responsavel.getEmail(), new ManutencaoRequest(sala.getId(), null, null, inicio, termino, "Manutencao preventiva"));
                return new OperationResult("manutencao", true, null);
            } catch (RuntimeException ex) {
                return new OperationResult("manutencao", false, ex.getClass().getSimpleName());
            }
        };
    }

    private String token(Usuario usuario) {
        return "Bearer " + jwtService.generateToken(usuario);
    }

    private record OperationResult(String name, boolean success, String error) { }
}
