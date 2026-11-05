package br.com.senac.organizacaorecursos;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.senac.organizacaorecursos.dto.ReservaRequest;
import br.com.senac.organizacaorecursos.entity.Perfil;
import br.com.senac.organizacaorecursos.entity.Sala;
import br.com.senac.organizacaorecursos.entity.Usuario;
import br.com.senac.organizacaorecursos.repository.AuditoriaRepository;
import br.com.senac.organizacaorecursos.repository.MaterialRepository;
import br.com.senac.organizacaorecursos.repository.NotificacaoRepository;
import br.com.senac.organizacaorecursos.repository.ProfessorRepository;
import br.com.senac.organizacaorecursos.repository.ReservaRepository;
import br.com.senac.organizacaorecursos.repository.SalaRepository;
import br.com.senac.organizacaorecursos.repository.UsuarioRepository;
import br.com.senac.organizacaorecursos.service.ReservaConflitoException;
import br.com.senac.organizacaorecursos.service.ReservaService;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("postgres-test")
@Testcontainers
class ReservaConcorrenciaPostgresTest {
    private static final String BCRYPT_HASH = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
    private static final OffsetDateTime INICIO = OffsetDateTime.parse("2026-11-03T09:00:00-03:00");
    private static final OffsetDateTime TERMINO = OffsetDateTime.parse("2026-11-03T10:00:00-03:00");

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

    @Autowired private ReservaService reservaService;
    @Autowired private ReservaRepository reservaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private SalaRepository salaRepository;
    @Autowired private ProfessorRepository professorRepository;
    @Autowired private MaterialRepository materialRepository;
    @Autowired private NotificacaoRepository notificacaoRepository;
    @Autowired private AuditoriaRepository auditoriaRepository;

    private Usuario solicitante;
    private Sala sala;
    private Sala outraSala;

    @BeforeEach
    void prepararDados() {
        notificacaoRepository.deleteAll();
        auditoriaRepository.deleteAll();
        reservaRepository.deleteAll();
        materialRepository.deleteAll();
        professorRepository.deleteAll();
        salaRepository.deleteAll();
        usuarioRepository.deleteAll();
        solicitante = usuarioRepository.saveAndFlush(
                new Usuario("Solicitante", "concorrencia@senac.br", BCRYPT_HASH, Perfil.SOLICITANTE, true));
        sala = salaRepository.saveAndFlush(new Sala("Sala 1", "Aula", 30, "Bloco A", false, true));
        outraSala = salaRepository.saveAndFlush(new Sala("Sala 2", "Aula", 30, "Bloco A", false, true));
    }

    @Test
    void somenteUmaReservaConcorrenteDoMesmoRecursoEIntervaloEConfirmada() throws Exception {
        List<Resultado> resultados = executarConcorrentemente(
                pedidoSala(sala), pedidoSala(sala));

        long sucessos = resultados.stream().filter(Resultado.CRIADA::equals).count();
        long conflitos = resultados.stream().filter(Resultado.CONFLITO::equals).count();
        assertThat(sucessos).isEqualTo(1);
        assertThat(conflitos).isEqualTo(1);

        var reservasPersistidas = reservaRepository.findAll().stream()
                .filter(reserva -> reserva.getSala().getId().equals(sala.getId()))
                .toList();
        assertThat(reservasPersistidas).hasSize(1);
        assertThat(reservasPersistidas.getFirst().getInicio()).isEqualTo(INICIO);
        assertThat(reservasPersistidas.getFirst().getTermino()).isEqualTo(TERMINO);
    }

    @Test
    void reservasConcorrentesDeRecursosDiferentesPodemSerConfirmadas() throws Exception {
        List<Resultado> resultados = executarConcorrentemente(
                pedidoSala(sala), pedidoSala(outraSala));

        assertThat(resultados).containsOnly(Resultado.CRIADA);
        assertThat(reservaRepository.findAll()).hasSize(2);
        assertThat(reservaRepository.findAll().stream()
                .map(reserva -> reserva.getSala().getId()).distinct()).hasSize(2);
    }

    private List<Resultado> executarConcorrentemente(ReservaRequest... pedidos) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(pedidos.length);
        CountDownLatch prontos = new CountDownLatch(pedidos.length);
        CountDownLatch iniciar = new CountDownLatch(1);
        List<Future<Resultado>> futuros = new ArrayList<>();
        try {
            for (ReservaRequest pedido : pedidos) {
                futuros.add(executor.submit(() -> {
                    prontos.countDown();
                    if (!iniciar.await(10, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Barreira de concorrência expirou");
                    }
                    try {
                        reservaService.criar(solicitante.getEmail(), pedido);
                        return Resultado.CRIADA;
                    } catch (ReservaConflitoException conflito) {
                        return Resultado.CONFLITO;
                    }
                }));
            }
            assertThat(prontos.await(10, TimeUnit.SECONDS)).isTrue();
            iniciar.countDown();
            return futuros.stream().map(futuro -> obterResultado(futuro)).toList();
        } finally {
            iniciar.countDown();
            executor.shutdownNow();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }
    }

    private Resultado obterResultado(Future<Resultado> futuro) {
        try {
            return futuro.get(20, TimeUnit.SECONDS);
        } catch (Exception erro) {
            throw new AssertionError("Operação concorrente falhou", erro);
        }
    }

    private ReservaRequest pedidoSala(Sala recurso) {
        return new ReservaRequest(recurso.getId(), null, null, INICIO, TERMINO);
    }

    private enum Resultado { CRIADA, CONFLITO }
}
