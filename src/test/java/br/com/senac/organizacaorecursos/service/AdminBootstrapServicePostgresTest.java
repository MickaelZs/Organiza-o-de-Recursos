package br.com.senac.organizacaorecursos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.senac.organizacaorecursos.entity.Perfil;
import br.com.senac.organizacaorecursos.entity.Usuario;
import br.com.senac.organizacaorecursos.repository.UsuarioRepository;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("postgres-test")
@Testcontainers(disabledWithoutDocker = true)
class AdminBootstrapServicePostgresTest {
    @Container
    static final PostgreSQLContainer<?> postgresql = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("admin_bootstrap_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresql::getJdbcUrl);
        registry.add("spring.datasource.username", postgresql::getUsername);
        registry.add("spring.datasource.password", postgresql::getPassword);
    }

    @Autowired
    private AdminBootstrapService bootstrapService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @BeforeEach
    void limparUsuariosDoTeste() {
        usuarioRepository.deleteAllInBatch();
    }

    @Test
    void criaPrimeiroAdminComHashBcryptEPermiteLogin() {
        char[] senha = "UmaSenhaForte#2026".toCharArray();

        bootstrapService.criarAdministradorInicial("Ana Silva", "ana@senac.br", senha);

        Usuario admin = usuarioRepository.findByEmail("ana@senac.br").orElseThrow();
        assertThat(admin.getPerfil()).isEqualTo(Perfil.ADMIN);
        assertThat(admin.getAtivo()).isTrue();
        assertThat(admin.getSenha()).startsWith("$2");
        assertThat(admin.getSenha()).isNotEqualTo(new String(senha));
        assertThat(passwordEncoder.matches(new String(senha), admin.getSenha())).isTrue();
        assertThat(authService.login("ana@senac.br", new String(senha))).isNotBlank();
        assertThatThrownBy(() -> authService.login("ana@senac.br", "senha-incorreta"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void rejeitaSenhaFracaSemCriarUsuario() {
        assertThatThrownBy(() -> bootstrapService.criarAdministradorInicial(
                "Ana Silva", "ana@senac.br", "curta".toCharArray()))
                .isInstanceOf(AdminBootstrapException.class);

        assertThat(usuarioRepository.count()).isZero();
    }

    @Test
    void rejeitaEmailInvalidoSemCriarUsuario() {
        assertThatThrownBy(() -> bootstrapService.criarAdministradorInicial(
                "Ana Silva", "email-invalido", "UmaSenhaForte#2026".toCharArray()))
                .isInstanceOf(AdminBootstrapException.class);

        assertThat(usuarioRepository.count()).isZero();
    }

    @Test
    void rejeitaSenhaNulaSemCriarUsuario() {
        assertThatThrownBy(() -> bootstrapService.criarAdministradorInicial(
                "Ana Silva", "ana@senac.br", null))
                .isInstanceOf(AdminBootstrapException.class)
                .hasMessageContaining("senha");

        assertThat(usuarioRepository.count()).isZero();
    }

    @Test
    void rejeitaNomeOuEmailNuloSemCriarUsuario() {
        assertThatThrownBy(() -> bootstrapService.criarAdministradorInicial(
                null, "ana@senac.br", "UmaSenhaForte#2026".toCharArray()))
                .isInstanceOf(AdminBootstrapException.class);
        assertThatThrownBy(() -> bootstrapService.criarAdministradorInicial(
                "Ana Silva", null, "UmaSenhaForte#2026".toCharArray()))
                .isInstanceOf(AdminBootstrapException.class);

        assertThat(usuarioRepository.count()).isZero();
    }

    @Test
    void rejeitaSenhaAcimaDoLimiteDoBcryptSemCriarUsuario() {
        char[] senhaAcimaDoLimite = "a".repeat(73).toCharArray();

        assertThatThrownBy(() -> bootstrapService.criarAdministradorInicial(
                "Ana Silva", "ana@senac.br", senhaAcimaDoLimite))
                .isInstanceOf(AdminBootstrapException.class)
                .hasMessageContaining("BCrypt");

        assertThat(usuarioRepository.count()).isZero();
    }

    @Test
    void bloqueiaInicializacaoQuandoJaExisteUsuarioSemAlterarOsDados() {
        Usuario existente = usuarioRepository.saveAndFlush(new Usuario(
                "Solicitante", "admin@senac.br", passwordEncoder.encode("SenhaExistente#2026"),
                Perfil.SOLICITANTE, true));

        assertThatThrownBy(() -> bootstrapService.criarAdministradorInicial(
                "Admin", "admin@senac.br", "UmaSenhaForte#2026".toCharArray()))
                .isInstanceOf(AdminBootstrapException.class)
                .hasMessageContaining("já existe usuário");

        assertThat(usuarioRepository.count()).isEqualTo(1);
        assertThat(usuarioRepository.findById(existente.getId()).orElseThrow().getPerfil())
                .isEqualTo(Perfil.SOLICITANTE);
    }

    @Test
    void recusaExecucaoRepetidaSemDuplicarAdministrador() {
        bootstrapService.criarAdministradorInicial(
                "Ana Silva", "ana@senac.br", "UmaSenhaForte#2026".toCharArray());

        assertThatThrownBy(() -> bootstrapService.criarAdministradorInicial(
                "Ana Silva", "ana@senac.br", "UmaSenhaForte#2026".toCharArray()))
                .isInstanceOf(AdminBootstrapException.class);

        assertThat(usuarioRepository.count()).isEqualTo(1);
    }

    @Test
    void execucoesConcorrentesCriamNoMaximoUmAdministrador() throws Exception {
        CountDownLatch inicio = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> primeira = executor.submit(() -> tentarCriarAdmin(inicio));
            Future<Boolean> segunda = executor.submit(() -> tentarCriarAdmin(inicio));
            inicio.countDown();

            assertThat(java.util.List.of(primeira.get(), segunda.get()))
                    .containsExactlyInAnyOrder(true, false);
            assertThat(usuarioRepository.count()).isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }
    }

    private boolean tentarCriarAdmin(CountDownLatch inicio) throws InterruptedException {
        inicio.await();
        try {
            bootstrapService.criarAdministradorInicial(
                    "Ana Silva", "ana@senac.br", "UmaSenhaForte#2026".toCharArray());
            return true;
        } catch (AdminBootstrapException exception) {
            return false;
        }
    }
}
