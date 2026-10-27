package br.com.senac.organizacaorecursos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.senac.organizacaorecursos.entity.Material;
import br.com.senac.organizacaorecursos.repository.MaterialRepository;
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
class MaterialRepositoryPostgresTest {
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
    private MaterialRepository materialRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void persisteMaterialComQuantidadeERestrito() {
        Material material = materialRepository.saveAndFlush(
                new Material("Projetor", "Equipamento", 5, 3, "Almoxarifado", true, true));

        assertThat(material.getId()).isNotNull();
        assertThat(jdbcTemplate.queryForObject("SELECT quantidade_disponivel FROM materiais WHERE id = ?", Integer.class,
                material.getId())).isEqualTo(3);
    }

    @Test
    void rejeitaQuantidadeTotalNegativa() {
        assertThatThrownBy(() -> inserirMaterial(-1, 0)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejeitaQuantidadeDisponivelNegativa() {
        assertThatThrownBy(() -> inserirMaterial(1, -1)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejeitaQuantidadeDisponivelMaiorQueTotal() {
        assertThatThrownBy(() -> inserirMaterial(1, 2)).isInstanceOf(DataIntegrityViolationException.class);
    }

    private void inserirMaterial(int quantidadeTotal, int quantidadeDisponivel) {
        jdbcTemplate.update("""
                INSERT INTO materiais (nome, tipo, quantidade_total, quantidade_disponivel, localizacao, restrito, ativo)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, "Projetor", "Equipamento", quantidadeTotal, quantidadeDisponivel,
                "Almoxarifado", false, true);
    }
}
