package br.com.senac.organizacaorecursos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.senac.organizacaorecursos.entity.Sala;
import br.com.senac.organizacaorecursos.repository.SalaRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
class SalaRepositoryPostgresTest {
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
    private SalaRepository salaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void persisteSalaComTodosOsCampos() {
        Sala sala = salaRepository.saveAndFlush(new Sala("Laboratorio 1", "Laboratorio", 30,
                "Bloco A", false, true));

        assertThat(sala.getId()).isNotNull();
        assertThat(jdbcTemplate.queryForObject("SELECT capacidade FROM salas WHERE id = ?", Integer.class,
                sala.getId())).isEqualTo(30);
    }

    @Test
    void rejeitaCapacidadeInvalida() {
        assertThatThrownBy(() -> salaRepository.saveAndFlush(new Sala("Laboratorio 1", "Laboratorio", 0,
                "Bloco A", false, true))).isInstanceOf(ConstraintViolationException.class);
    }
}
