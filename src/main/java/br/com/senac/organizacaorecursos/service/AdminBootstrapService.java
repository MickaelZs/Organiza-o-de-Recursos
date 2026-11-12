package br.com.senac.organizacaorecursos.service;

import br.com.senac.organizacaorecursos.dto.AdminBootstrapRequest;
import br.com.senac.organizacaorecursos.entity.Perfil;
import br.com.senac.organizacaorecursos.entity.Usuario;
import br.com.senac.organizacaorecursos.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.util.Comparator;
import java.util.Set;
import org.hibernate.Session;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("!test")
public class AdminBootstrapService {
    private static final int BOOTSTRAP_LOCK_NAMESPACE = 0x53454E41;
    private static final int BOOTSTRAP_LOCK_KEY = 1;
    private static final int BCRYPT_MAX_PASSWORD_BYTES = 72;

    private final EntityManager entityManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;

    public AdminBootstrapService(EntityManager entityManager, UsuarioRepository usuarioRepository,
                                 PasswordEncoder passwordEncoder, Validator validator) {
        this.entityManager = entityManager;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.validator = validator;
    }

    @Transactional
    public void criarAdministradorInicial(String nome, String email, char[] senha) {
        if (senha == null) {
            throw new AdminBootstrapException("A senha é obrigatória.");
        }

        String nomeNormalizado = nome == null ? null : nome.strip();
        String emailNormalizado = email == null ? null : email.strip();
        CharBuffer senhaBuffer = CharBuffer.wrap(senha);
        AdminBootstrapRequest request = new AdminBootstrapRequest(nomeNormalizado, emailNormalizado, senhaBuffer);
        Set<ConstraintViolation<AdminBootstrapRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String mensagens = violations.stream()
                    .sorted(Comparator.comparing(violation -> violation.getPropertyPath().toString()))
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .distinct()
                    .reduce((first, second) -> first + "; " + second)
                    .orElse("dados inválidos");
            throw new AdminBootstrapException("Dados do administrador inválidos: " + mensagens);
        }

        int tamanhoSenhaBytes = StandardCharsets.UTF_8.encode(CharBuffer.wrap(senha)).remaining();
        if (tamanhoSenhaBytes > BCRYPT_MAX_PASSWORD_BYTES) {
            throw new AdminBootstrapException("A senha excede o limite suportado pelo BCrypt.");
        }

        adquirirLockDeBootstrap();
        if (usuarioRepository.count() != 0) {
            throw new AdminBootstrapException(
                    "Inicialização bloqueada: já existe usuário neste banco. Nenhum dado foi alterado.");
        }

        String hash = passwordEncoder.encode(CharBuffer.wrap(senha));
        usuarioRepository.saveAndFlush(
                new Usuario(nomeNormalizado, emailNormalizado, hash, Perfil.ADMIN, true));
    }

    private void adquirirLockDeBootstrap() {
        entityManager.unwrap(Session.class).doWork(connection -> {
            try (PreparedStatement statement =
                         connection.prepareStatement("SELECT pg_advisory_xact_lock(?, ?)")) {
                statement.setInt(1, BOOTSTRAP_LOCK_NAMESPACE);
                statement.setInt(2, BOOTSTRAP_LOCK_KEY);
                statement.execute();
            }
        });
    }
}
