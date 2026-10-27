package br.com.senac.organizacaorecursos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.senac.organizacaorecursos.entity.Professor;
import br.com.senac.organizacaorecursos.repository.ProfessorRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class ProfessorRepositoryPostgresTest {
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

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void persisteProfessorComCompetenciaComoTexto() {
        Professor professor = professorRepository.saveAndFlush(
                new Professor("Ana Silva", "ana.silva@senac.br", "Matematica", true));

        assertThat(professor.getId()).isNotNull();
        assertThat(jdbcTemplate.queryForObject("SELECT competencia FROM professores WHERE id = ?", String.class,
                professor.getId())).isEqualTo("Matematica");
    }

    @Test
    void rejeitaEmailDuplicadoNoPostgresql() {
        professorRepository.saveAndFlush(new Professor("Ana Silva", "ana.silva@senac.br", "Matematica", true));

        assertThatThrownBy(() -> professorRepository.saveAndFlush(
                new Professor("Outra Ana", "ana.silva@senac.br", "Fisica", true)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejeitaProfessorComCamposInvalidos() {
        assertThatThrownBy(() -> professorRepository.saveAndFlush(
                new Professor("", "invalido", "", null)))
                .isInstanceOf(ConstraintViolationException.class);
    }
}
