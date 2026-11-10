package br.com.senac.organizacaorecursos.cli;

import br.com.senac.organizacaorecursos.OrganizacaoRecursosApplication;
import br.com.senac.organizacaorecursos.service.AdminBootstrapService;
import java.io.Console;
import java.net.URI;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;

public final class AdminBootstrapCommand {
    private static final String JDBC_PREFIX = "jdbc:";
    private static final String POSTGRES_PREFIX = "jdbc:postgresql://";

    private AdminBootstrapCommand() {
    }

    public static void executar() {
        Console console = System.console();
        if (console == null) {
            throw new IllegalStateException(
                    "Este comando exige um terminal interativo para ler a senha sem exibi-la.");
        }

        String databaseUrl = System.getenv("DB_URL");
        validarUrlLocal(databaseUrl);

        String usuarioBanco = System.getenv("DB_USERNAME");
        if (usuarioBanco == null || usuarioBanco.isBlank()) {
            usuarioBanco = console.readLine("Usuário do PostgreSQL: ");
        }
        if (usuarioBanco == null || usuarioBanco.isBlank()) {
            throw new IllegalArgumentException("O usuário do PostgreSQL é obrigatório.");
        }

        String nome = console.readLine("Nome do administrador: ");
        String email = console.readLine("E-mail do administrador: ");
        char[] senha = console.readPassword("Senha do administrador (mínimo 12 caracteres): ");
        char[] confirmacao = console.readPassword("Confirme a senha: ");
        char[] senhaBanco = console.readPassword("Senha do PostgreSQL (pode ficar vazia se o banco usar trust): ");

        try {
            if (senha == null || confirmacao == null || !Arrays.equals(senha, confirmacao)) {
                throw new IllegalArgumentException("As senhas não conferem.");
            }

            verificarBancoVazio(databaseUrl, usuarioBanco, senhaBanco);
            iniciarAplicacao(databaseUrl, usuarioBanco, senhaBanco, nome, email, senha);
            console.printf("Administrador inicial criado. Encerre este processo e inicie a aplicação normalmente.%n");
        } finally {
            limpar(senha);
            limpar(confirmacao);
            limpar(senhaBanco);
        }
    }

    static void validarUrlLocal(String databaseUrl) {
        if (databaseUrl == null || !databaseUrl.startsWith(POSTGRES_PREFIX)) {
            throw new IllegalArgumentException(
                    "Defina DB_URL explicitamente como uma URL JDBC PostgreSQL local.");
        }

        try {
            URI uri = URI.create(databaseUrl.substring(JDBC_PREFIX.length()));
            String host = uri.getHost();
            boolean local = "localhost".equalsIgnoreCase(host)
                    || "127.0.0.1".equals(host)
                    || "::1".equals(host)
                    || "[::1]".equals(host);
            String path = uri.getRawPath();
            if (!local || uri.getPort() < 1 || uri.getPort() > 65535
                    || uri.getRawUserInfo() != null || uri.getRawQuery() != null
                    || uri.getRawFragment() != null || path == null
                    || !path.matches("/[A-Za-z0-9_$-]+")) {
                throw new IllegalArgumentException(
                        "O bootstrap aceita apenas um banco PostgreSQL local explicitamente selecionado, sem credenciais na URL.");
            }
        } catch (IllegalArgumentException exception) {
            if (exception.getMessage() != null && exception.getMessage().startsWith("O bootstrap")) {
                throw exception;
            }
            throw new IllegalArgumentException("DB_URL não é uma URL JDBC PostgreSQL local válida.");
        }
    }

    private static void verificarBancoVazio(String databaseUrl, String usuarioBanco, char[] senhaBanco) {
        String senha = new String(senhaBanco == null ? new char[0] : senhaBanco);
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM pg_catalog.pg_namespace
                    WHERE nspname NOT IN ('pg_catalog', 'information_schema', 'public', 'pg_toast')
                      AND nspname NOT LIKE 'pg_temp_%'
                      AND nspname NOT LIKE 'pg_toast_temp_%'
                ) OR EXISTS (
                    SELECT 1
                    FROM pg_catalog.pg_class c
                    JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
                    WHERE n.nspname = 'public'
                ) OR EXISTS (
                    SELECT 1
                    FROM pg_catalog.pg_proc p
                    JOIN pg_catalog.pg_namespace n ON n.oid = p.pronamespace
                    WHERE n.nspname = 'public'
                ) OR EXISTS (
                    SELECT 1
                    FROM pg_catalog.pg_type t
                    JOIN pg_catalog.pg_namespace n ON n.oid = t.typnamespace
                    WHERE n.nspname = 'public'
                      AND t.typtype IN ('c', 'd', 'e', 'r')
                )
                """;
        try (Connection connection = DriverManager.getConnection(databaseUrl, usuarioBanco, senha);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next() || resultSet.getBoolean(1)) {
                throw new IllegalStateException(
                        "Bootstrap cancelado: o banco não está vazio. Nenhuma migration ou alteração foi executada.");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível verificar o banco local. Confirme a URL, o usuário e a senha do PostgreSQL.");
        }
    }

    private static void iniciarAplicacao(String databaseUrl, String usuarioBanco, char[] senhaBanco,
                                         String nome, String email, char[] senha) {
        byte[] jwtSecretBytes = new byte[32];
        new SecureRandom().nextBytes(jwtSecretBytes);
        String jwtSecret = Base64.getEncoder().encodeToString(jwtSecretBytes);
        Arrays.fill(jwtSecretBytes, (byte) 0);

        Map<String, Object> bootstrapProperties = Map.of(
                "spring.datasource.url", databaseUrl,
                "spring.datasource.username", usuarioBanco,
                "spring.datasource.password", new String(senhaBanco == null ? new char[0] : senhaBanco),
                "spring.flyway.url", databaseUrl,
                "spring.flyway.user", usuarioBanco,
                "spring.flyway.password", new String(senhaBanco == null ? new char[0] : senhaBanco),
                "app.jwt.secret", jwtSecret,
                "server.address", "127.0.0.1",
                "server.port", "0");

        SpringApplication application = new SpringApplication(OrganizacaoRecursosApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.addInitializers(context -> context.getEnvironment().getPropertySources()
                .addFirst(new MapPropertySource("adminBootstrapProperties", bootstrapProperties)));

        try (ConfigurableApplicationContext context = application.run()) {
            context.getBean(AdminBootstrapService.class).criarAdministradorInicial(nome, email, senha);
        }
    }

    private static void limpar(char[] valor) {
        if (valor != null) {
            Arrays.fill(valor, '\0');
        }
    }
}
