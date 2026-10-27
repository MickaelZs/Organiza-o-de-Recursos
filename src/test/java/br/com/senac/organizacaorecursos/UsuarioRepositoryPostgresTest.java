package br.com.senac.organizacaorecursos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.senac.organizacaorecursos.entity.Perfil;
import br.com.senac.organizacaorecursos.entity.Usuario;
import br.com.senac.organizacaorecursos.repository.UsuarioRepository;
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
class UsuarioRepositoryPostgresTest {
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
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void persisteUsuarioComPerfilComoTextoEBuscaPorEmail() {
        Usuario usuario = usuarioRepository.saveAndFlush(
                new Usuario("Ana Silva", "ana.silva@senac.br", "hash-futuro", Perfil.RESPONSAVEL, true));

        Usuario encontrado = usuarioRepository.findByEmail("ana.silva@senac.br").orElseThrow();

        assertThat(encontrado.getId()).isEqualTo(usuario.getId());
        assertThat(encontrado.getPerfil()).isEqualTo(Perfil.RESPONSAVEL);
        assertThat(encontrado.getCreatedAt()).isNotNull();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT perfil FROM usuarios WHERE id = ?", String.class, usuario.getId()))
                .isEqualTo("RESPONSAVEL");
    }

    @Test
    void rejeitaEmailDuplicadoNoPostgresql() {
        usuarioRepository.saveAndFlush(
                new Usuario("Ana Silva", "duplicado@senac.br", "hash-futuro", Perfil.SOLICITANTE, true));

        assertThatThrownBy(() -> usuarioRepository.saveAndFlush(
                new Usuario("Outra Ana", "duplicado@senac.br", "outro-hash", Perfil.ADMIN, false)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejeitaCamposEstruturaisInvalidos() {
        assertThatThrownBy(() -> usuarioRepository.saveAndFlush(
                new Usuario("", "invalido@senac.br", "", null, null)))
                .isInstanceOf(ConstraintViolationException.class);
    }
}
