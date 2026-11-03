package br.com.senac.organizacaorecursos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.senac.organizacaorecursos.entity.Material;
import br.com.senac.organizacaorecursos.entity.Perfil;
import br.com.senac.organizacaorecursos.entity.Professor;
import br.com.senac.organizacaorecursos.entity.Reserva;
import br.com.senac.organizacaorecursos.entity.Sala;
import br.com.senac.organizacaorecursos.entity.StatusReserva;
import br.com.senac.organizacaorecursos.entity.Usuario;
import br.com.senac.organizacaorecursos.repository.MaterialRepository;
import br.com.senac.organizacaorecursos.repository.ProfessorRepository;
import br.com.senac.organizacaorecursos.repository.ReservaRepository;
import br.com.senac.organizacaorecursos.repository.SalaRepository;
import br.com.senac.organizacaorecursos.repository.UsuarioRepository;
import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("postgres-test")
@Testcontainers
class ReservaRepositoryPostgresTest {
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

    @Autowired private ReservaRepository reservaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private SalaRepository salaRepository;
    @Autowired private ProfessorRepository professorRepository;
    @Autowired private MaterialRepository materialRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    private Usuario solicitante;
    private Sala sala;
    private Professor professor;
    private Material material;

    @BeforeEach
    void preparaReferencias() {
        reservaRepository.deleteAll();
        materialRepository.deleteAll();
        professorRepository.deleteAll();
        salaRepository.deleteAll();
        usuarioRepository.deleteAll();
        solicitante = usuarioRepository.saveAndFlush(
                new Usuario("Ana Silva", "ana.silva@senac.br", BCRYPT_HASH, Perfil.SOLICITANTE, true));
        sala = salaRepository.saveAndFlush(new Sala("Sala 1", "Aula", 30, "Bloco A", false, true));
        professor = professorRepository.saveAndFlush(
                new Professor("Carlos Souza", "carlos.souza@senac.br", "Matematica", true));
        material = materialRepository.saveAndFlush(
                new Material("Projetor", "Equipamento", 5, 5, "Almoxarifado", false, true));
    }

    @Test
    void persisteReservaDeSalaComStatusComoTexto() {
        Reserva salva = reservaRepository.saveAndFlush(reserva(sala, null, null,
                StatusReserva.SOLICITADA, OffsetDateTime.parse("2026-11-03T09:00:00-03:00"),
                OffsetDateTime.parse("2026-11-03T10:00:00-03:00")));

        Reserva encontrada = reler(salva);
        assertVinculoSala(encontrada);
        assertThat(encontrada.getStatus()).isEqualTo(StatusReserva.SOLICITADA);
        assertThat(jdbcTemplate.queryForObject("SELECT status FROM reservas WHERE id = ?", String.class, salva.getId()))
                .isEqualTo("SOLICITADA");
    }

    @Test
    void persisteERetornaReservaDeProfessor() {
        Reserva salva = reservaRepository.saveAndFlush(reserva(null, professor, null,
                StatusReserva.APROVADA, inicio(), termino()));

        assertVinculoProfessor(reler(salva));
    }

    @Test
    void persisteERetornaReservaDeMaterial() {
        Reserva salva = reservaRepository.saveAndFlush(reserva(null, null, material,
                StatusReserva.NAO_COMPARECEU, inicio(), termino()));

        assertVinculoMaterial(reler(salva));
    }

    @Test
    void rejeitaReservaSemRecursoNaAplicacao() {
        assertViolacaoAplicacao(reserva(null, null, null, StatusReserva.SOLICITADA, inicio(), termino()));
    }

    @Test
    void rejeitaReservaComMultiplosRecursosNaAplicacao() {
        assertViolacaoAplicacao(reserva(sala, professor, null, StatusReserva.SOLICITADA, inicio(), termino()));
    }

    @Test
    void rejeitaSolicitanteAusenteNaAplicacao() {
        assertViolacaoAplicacao(new Reserva(null, sala, null, null,
                inicio(), termino(), StatusReserva.SOLICITADA));
    }

    @Test
    void rejeitaInicioAusenteNaAplicacao() {
        assertViolacaoAplicacao(reserva(sala, null, null, StatusReserva.SOLICITADA, null, termino()));
    }

    @Test
    void rejeitaTerminoAusenteNaAplicacao() {
        assertViolacaoAplicacao(reserva(sala, null, null, StatusReserva.SOLICITADA, inicio(), null));
    }

    @Test
    void rejeitaStatusAusenteNaAplicacao() {
        assertViolacaoAplicacao(reserva(sala, null, null, null, inicio(), termino()));
    }

    @Test
    void rejeitaTerminoIgualAoInicioNaAplicacao() {
        assertViolacaoAplicacao(reserva(sala, null, null, StatusReserva.SOLICITADA, inicio(), inicio()));
    }

    @Test
    void rejeitaTerminoAnteriorAoInicioNaAplicacao() {
        assertViolacaoAplicacao(reserva(sala, null, null, StatusReserva.SOLICITADA,
                termino(), inicio()));
    }

    @Test
    void rejeitaNoBancoReservaSemRecursoOuComMaisDeUmRecurso() {
        assertThatThrownBy(() -> inserirReserva(null, null, null, inicio(), termino()))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> inserirReserva(sala.getId(), professor.getId(), null, inicio(), termino()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejeitaTerminoNaoPosteriorAoInicioNoBanco() {
        assertThatThrownBy(() -> inserirReserva(sala.getId(), null, null, inicio(), inicio()))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> inserirReserva(sala.getId(), null, null, termino(), inicio()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejeitaSolicitanteInexistentePelaChaveEstrangeira() {
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO reservas (solicitante_id, sala_id, inicio, termino, status)
                VALUES (?, ?, ?, ?, ?)
                """, Long.MAX_VALUE, sala.getId(), inicio(), termino(), "SOLICITADA"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Reserva reserva(Sala salaReserva, Professor professorReserva, Material materialReserva,
                            StatusReserva status, OffsetDateTime inicio, OffsetDateTime termino) {
        return new Reserva(solicitante, salaReserva, professorReserva, materialReserva, inicio, termino, status);
    }

    private Reserva reler(Reserva salva) {
        return reservaRepository.findById(salva.getId()).orElseThrow();
    }

    private void assertViolacaoAplicacao(Reserva reserva) {
        assertThatThrownBy(() -> reservaRepository.saveAndFlush(reserva))
                .isInstanceOf(ConstraintViolationException.class);
    }

    private void assertVinculoSala(Reserva reserva) {
        assertThat(reserva.getSolicitante().getId()).isEqualTo(solicitante.getId());
        assertThat(reserva.getSala().getId()).isEqualTo(sala.getId());
        assertThat(reserva.getProfessor()).isNull();
        assertThat(reserva.getMaterial()).isNull();
    }

    private void assertVinculoProfessor(Reserva reserva) {
        assertThat(reserva.getSolicitante().getId()).isEqualTo(solicitante.getId());
        assertThat(reserva.getSala()).isNull();
        assertThat(reserva.getProfessor().getId()).isEqualTo(professor.getId());
        assertThat(reserva.getMaterial()).isNull();
    }

    private void assertVinculoMaterial(Reserva reserva) {
        assertThat(reserva.getSolicitante().getId()).isEqualTo(solicitante.getId());
        assertThat(reserva.getSala()).isNull();
        assertThat(reserva.getProfessor()).isNull();
        assertThat(reserva.getMaterial().getId()).isEqualTo(material.getId());
    }

    private void inserirReserva(Long salaId, Long professorId, Long materialId,
                                OffsetDateTime inicio, OffsetDateTime termino) {
        jdbcTemplate.update("""
                INSERT INTO reservas (solicitante_id, sala_id, professor_id, material_id, inicio, termino, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, solicitante.getId(), salaId, professorId, materialId, inicio, termino, "SOLICITADA");
    }

    private OffsetDateTime inicio() { return OffsetDateTime.parse("2026-11-03T09:00:00-03:00"); }
    private OffsetDateTime termino() { return OffsetDateTime.parse("2026-11-03T10:00:00-03:00"); }
}
