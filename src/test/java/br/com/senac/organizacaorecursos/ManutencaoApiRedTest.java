package br.com.senac.organizacaorecursos;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.senac.organizacaorecursos.entity.Material;
import br.com.senac.organizacaorecursos.entity.Perfil;
import br.com.senac.organizacaorecursos.entity.Professor;
import br.com.senac.organizacaorecursos.entity.Sala;
import br.com.senac.organizacaorecursos.entity.Usuario;
import br.com.senac.organizacaorecursos.repository.MaterialRepository;
import br.com.senac.organizacaorecursos.repository.ProfessorRepository;
import br.com.senac.organizacaorecursos.repository.ReservaRepository;
import br.com.senac.organizacaorecursos.repository.SalaRepository;
import br.com.senac.organizacaorecursos.repository.UsuarioRepository;
import br.com.senac.organizacaorecursos.security.JwtService;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
class ManutencaoApiRedTest {
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
    @Autowired private ReservaRepository reservaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private SalaRepository salaRepository;
    @Autowired private ProfessorRepository professorRepository;
    @Autowired private MaterialRepository materialRepository;

    private Usuario solicitante;
    private Usuario responsavel;
    private Usuario admin;
    private Sala sala;
    private Professor professor;
    private Material material;

    @BeforeEach
    void preparaBanco() {
        reservaRepository.deleteAll();
        materialRepository.deleteAll();
        professorRepository.deleteAll();
        salaRepository.deleteAll();
        usuarioRepository.deleteAll();

        solicitante = salvarUsuario("solicitante@senac.br", Perfil.SOLICITANTE);
        responsavel = salvarUsuario("responsavel@senac.br", Perfil.RESPONSAVEL);
        admin = salvarUsuario("admin@senac.br", Perfil.ADMIN);

        sala = salaRepository.saveAndFlush(new Sala("Sala de manutencao", "Aula", 35, "Bloco A", false, true));
        professor = professorRepository.saveAndFlush(
                new Professor("Prof. Manutencao", "prof-manutencao@senac.br", "Infraestrutura", true));
        material = materialRepository.saveAndFlush(
                new Material("Projetor de manutencao", "Equipamento", 3, 3, "Almoxarifado", false, true));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("recursosValidos")
    void responsavelPodeCriarManutencaoParaCadaTipoDeRecurso(String recurso, String campo) throws Exception {
        mockMvc.perform(post("/api/manutencoes")
                        .header("Authorization", token(responsavel))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(manutencaoJson(campo, idDoRecurso(campo), "2026-11-03T10:00:00-03:00", "2026-11-03T12:00:00-03:00")))
                .andExpect(status().isCreated());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("recursosValidos")
    void adminPodeCriarManutencaoParaCadaTipoDeRecurso(String recurso, String campo) throws Exception {
        mockMvc.perform(post("/api/manutencoes")
                        .header("Authorization", token(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(manutencaoJson(campo, idDoRecurso(campo), "2026-11-03T10:00:00-03:00", "2026-11-03T12:00:00-03:00")))
                .andExpect(status().isCreated());
    }

    @Test
    void solicitanteNaoPodeCriarManutencao() throws Exception {
        mockMvc.perform(post("/api/manutencoes")
                        .header("Authorization", token(solicitante))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(manutencaoJson("salaId", sala.getId(), "2026-11-03T10:00:00-03:00", "2026-11-03T12:00:00-03:00")))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @MethodSource("intervalosInvalidos")
    void rejeitaManutencaoComIntervaloInvalido(String inicio, String termino) throws Exception {
        mockMvc.perform(post("/api/manutencoes")
                        .header("Authorization", token(responsavel))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(manutencaoJson("salaId", sala.getId(), inicio, termino)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reservaExistenteRejeitaManutencaoSobreposta() throws Exception {
        mockMvc.perform(post("/api/reservas")
                        .header("Authorization", token(solicitante))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservaJson("salaId", sala.getId(), "2026-11-03T10:00:00-03:00", "2026-11-03T12:00:00-03:00")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/manutencoes")
                        .header("Authorization", token(responsavel))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(manutencaoJson("salaId", sala.getId(), "2026-11-03T11:00:00-03:00", "2026-11-03T13:00:00-03:00")))
                .andExpect(status().isConflict());
    }

    @Test
    void manutencaoExistenteRejeitaReservaSobreposta() throws Exception {
        mockMvc.perform(post("/api/manutencoes")
                        .header("Authorization", token(responsavel))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(manutencaoJson("salaId", sala.getId(), "2026-11-03T10:00:00-03:00", "2026-11-03T12:00:00-03:00")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/reservas")
                        .header("Authorization", token(solicitante))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservaJson("salaId", sala.getId(), "2026-11-03T11:00:00-03:00", "2026-11-03T13:00:00-03:00")))
                .andExpect(status().isConflict());
    }

    @Test
    void manutencaoAdjacenteAoHorarioDeReservaDeveSerPermitida() throws Exception {
        mockMvc.perform(post("/api/reservas")
                        .header("Authorization", token(solicitante))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservaJson("salaId", sala.getId(), "2026-11-03T10:00:00-03:00", "2026-11-03T11:00:00-03:00")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/manutencoes")
                        .header("Authorization", token(responsavel))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(manutencaoJson("salaId", sala.getId(), "2026-11-03T11:00:00-03:00", "2026-11-03T12:00:00-03:00")))
                .andExpect(status().isCreated());
    }

    @Test
    void recursoInexistenteDeveRetornar404NaCriacaoDeManutencao() throws Exception {
        mockMvc.perform(post("/api/manutencoes")
                        .header("Authorization", token(responsavel))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(manutencaoJson("salaId", 999L, "2026-11-03T10:00:00-03:00", "2026-11-03T12:00:00-03:00")))
                .andExpect(status().isNotFound());
    }

    private static Stream<Arguments> recursosValidos() {
        return Stream.of(
                Arguments.of("Sala", "salaId"),
                Arguments.of("Professor", "professorId"),
                Arguments.of("Material", "materialId"));
    }

    private static Stream<Arguments> intervalosInvalidos() {
        return Stream.of(
                Arguments.of("2026-11-03T10:00:00-03:00", "2026-11-03T10:00:00-03:00"),
                Arguments.of("2026-11-03T12:00:00-03:00", "2026-11-03T10:00:00-03:00"));
    }

    private Usuario salvarUsuario(String email, Perfil perfil) {
        return usuarioRepository.saveAndFlush(new Usuario(
                perfil.name().toLowerCase(), email, BCRYPT_HASH, perfil, true));
    }

    private String token(Perfil perfil) {
        return "Bearer " + jwtService.generateToken(new Usuario("Token", "token@senac.br", BCRYPT_HASH, perfil, true));
    }

    private String token(Usuario usuario) {
        return "Bearer " + jwtService.generateToken(usuario);
    }

    private String manutencaoJson(String campo, Long recursoId, String inicio, String termino) {
        return "{\"" + campo + "\":" + recursoId + ",\"inicio\":\"" + inicio + "\",\"termino\":\"" + termino + "\",\"motivo\":\"Manutencao preventiva\"}";
    }

    private String reservaJson(String campo, Long recursoId, String inicio, String termino) {
        return "{\"" + campo + "\":" + recursoId + ",\"inicio\":\"" + inicio + "\",\"termino\":\"" + termino + "\"}";
    }

    private Long idDoRecurso(String campo) {
        return switch (campo) {
            case "salaId" -> sala.getId();
            case "professorId" -> professor.getId();
            case "materialId" -> material.getId();
            default -> throw new IllegalArgumentException("Campo de recurso desconhecido: " + campo);
        };
    }
}
