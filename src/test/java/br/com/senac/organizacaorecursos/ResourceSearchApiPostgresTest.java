package br.com.senac.organizacaorecursos;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.senac.organizacaorecursos.entity.Material;
import br.com.senac.organizacaorecursos.entity.Perfil;
import br.com.senac.organizacaorecursos.entity.Professor;
import br.com.senac.organizacaorecursos.entity.Sala;
import br.com.senac.organizacaorecursos.entity.Usuario;
import br.com.senac.organizacaorecursos.repository.MaterialRepository;
import br.com.senac.organizacaorecursos.repository.ProfessorRepository;
import br.com.senac.organizacaorecursos.repository.SalaRepository;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ResourceSearchApiPostgresTest {
    private static final String JWT_SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

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
        registry.add("app.jwt.secret", () -> JWT_SECRET);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private SalaRepository salaRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @BeforeEach
    void prepararDados() {
        materialRepository.deleteAll();
        professorRepository.deleteAll();
        salaRepository.deleteAll();

        salaRepository.saveAll(Stream.of(
                new Sala("Sala alvo", "Laboratorio", 40, "Bloco A", false, true),
                new Sala("Sala pequena", "Laboratorio", 20, "Bloco B", false, true),
                new Sala("Auditorio", "Auditorio", 50, "Bloco A", false, true)).toList());
        professorRepository.saveAll(Stream.of(
                new Professor("Ana", "ana@senac.br", "Desenvolvimento Java", true),
                new Professor("Bruno", "bruno@senac.br", "Redes", true)).toList());
        materialRepository.saveAll(Stream.of(
                new Material("Projetor alvo", "Equipamento", 5, 3, "Bloco A", true, true),
                new Material("Projetor B", "Equipamento", 2, 2, "Bloco B", false, true),
                new Material("Cabo", "Acessorio", 10, 10, "Bloco A", false, true)).toList());
    }

    @Test
    void combinaFiltrosDeSalaComCapacidadeMinima() throws Exception {
        mockMvc.perform(get("/api/salas?tipo=laboratorio&capacidadeMinima=30&localizacao=bloco%20a")
                        .header("Authorization", token(Perfil.SOLICITANTE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Sala alvo"));
    }

    @Test
    void retornaListaVaziaParaFiltroDeSalaValidoSemResultado() throws Exception {
        mockMvc.perform(get("/api/salas?tipo=Inexistente").header("Authorization", token(Perfil.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void rejeitaCapacidadeMinimaNegativa() throws Exception {
        mockMvc.perform(get("/api/salas?capacidadeMinima=-1").header("Authorization", token(Perfil.ADMIN)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void filtraProfessorPorCompetenciaParcialIgnorandoMaiusculas() throws Exception {
        mockMvc.perform(get("/api/professores?competencia=JAVA").header("Authorization", token(Perfil.RESPONSAVEL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Ana"));
    }

    @Test
    void retornaListaVaziaParaCompetenciaInexistente() throws Exception {
        mockMvc.perform(get("/api/professores?competencia=robotica").header("Authorization", token(Perfil.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void combinaFiltrosDeMaterial() throws Exception {
        mockMvc.perform(get("/api/materiais?tipo=equipamento&localizacao=bloco%20a")
                        .header("Authorization", token(Perfil.SOLICITANTE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Projetor alvo"));
    }

    @Test
    void retornaListaVaziaParaFiltroDeMaterialValidoSemResultado() throws Exception {
        mockMvc.perform(get("/api/materiais?localizacao=Anexo").header("Authorization", token(Perfil.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @ParameterizedTest(name = "{0} autenticado consulta {1} com sucesso")
    @MethodSource("consultasPermitidas")
    void permiteBuscaParaTodosOsPerfisAutenticados(String perfil, String endpoint, Perfil papel) throws Exception {
        mockMvc.perform(get(endpoint).header("Authorization", token(papel)))
                .andExpect(status().isOk());
    }

    @ParameterizedTest(name = "anonimo recebe 401 ao consultar {0}")
    @MethodSource("endpointsDeBusca")
    void exigeAutenticacaoParaBusca(String endpoint) throws Exception {
        mockMvc.perform(get(endpoint)).andExpect(status().isUnauthorized());
    }

    private static Stream<Arguments> consultasPermitidas() {
        return Stream.of(Perfil.SOLICITANTE, Perfil.RESPONSAVEL, Perfil.ADMIN)
                .flatMap(perfil -> Stream.of("/api/salas?tipo=laboratorio", "/api/professores?competencia=java",
                                "/api/materiais?tipo=equipamento")
                        .map(endpoint -> Arguments.of(perfil.name(), endpoint, perfil)));
    }

    private static Stream<Arguments> endpointsDeBusca() {
        return Stream.of("/api/salas?tipo=laboratorio", "/api/professores?competencia=java",
                "/api/materiais?tipo=equipamento").map(Arguments::of);
    }

    private String token(Perfil perfil) {
        return "Bearer " + jwtService.generateToken(
                new Usuario("Usuario", perfil.name().toLowerCase() + "@senac.br", "hash", perfil, true));
    }
}
