package br.com.senac.organizacaorecursos;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;

import br.com.senac.organizacaorecursos.entity.Material;
import br.com.senac.organizacaorecursos.entity.Perfil;
import br.com.senac.organizacaorecursos.entity.Professor;
import br.com.senac.organizacaorecursos.entity.Sala;
import br.com.senac.organizacaorecursos.entity.Reserva;
import br.com.senac.organizacaorecursos.entity.StatusReserva;
import br.com.senac.organizacaorecursos.entity.Usuario;
import br.com.senac.organizacaorecursos.repository.AuditoriaRepository;
import br.com.senac.organizacaorecursos.repository.MaterialRepository;
import br.com.senac.organizacaorecursos.repository.NotificacaoRepository;
import br.com.senac.organizacaorecursos.repository.ProfessorRepository;
import br.com.senac.organizacaorecursos.repository.SalaRepository;
import br.com.senac.organizacaorecursos.repository.ReservaRepository;
import br.com.senac.organizacaorecursos.repository.UsuarioRepository;
import br.com.senac.organizacaorecursos.security.JwtService;
import java.time.OffsetDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
class ResourceSearchApiPostgresTest {
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

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    private Sala salaOcupada;
    private Sala salaLivre;
    private Professor professorOcupado;
    private Material materialOcupado;
    private Usuario solicitante;

    @BeforeEach
    void prepararDados() {
        notificacaoRepository.deleteAll();
        auditoriaRepository.deleteAll();
        reservaRepository.deleteAll();
        usuarioRepository.deleteAll();
        materialRepository.deleteAll();
        professorRepository.deleteAll();
        salaRepository.deleteAll();

        var salas = salaRepository.saveAll(Stream.of(
                new Sala("Sala alvo", "Laboratorio", 40, "Bloco A", false, true),
                new Sala("Sala pequena", "Laboratorio", 20, "Bloco B", false, true),
                new Sala("Auditorio", "Auditorio", 50, "Bloco A", false, true),
                new Sala("Sala livre", "Laboratorio", 35, "Bloco A", false, true)).toList());
        salaOcupada = salas.get(0);
        salaLivre = salas.get(3);
        var professores = professorRepository.saveAll(Stream.of(
                new Professor("Ana", "ana@senac.br", "Desenvolvimento Java", true),
                new Professor("Bruno", "bruno@senac.br", "Redes", true)).toList());
        professorOcupado = professores.get(0);
        var materiais = materialRepository.saveAll(Stream.of(
                new Material("Projetor alvo", "Equipamento", 5, 3, "Bloco A", true, true),
                new Material("Projetor B", "Equipamento", 2, 2, "Bloco B", false, true),
                new Material("Cabo", "Acessorio", 10, 10, "Bloco A", false, true)).toList());
        materialOcupado = materiais.get(0);
        solicitante = usuarioRepository.saveAndFlush(
                new Usuario("Solicitante", "disponibilidade@senac.br", BCRYPT_HASH, Perfil.SOLICITANTE, true));
    }

    @Test
    void combinaFiltrosDeSalaComCapacidadeMinima() throws Exception {
        mockMvc.perform(get("/api/salas").param("tipo", "laboratorio")
                        .param("capacidadeMinima", "30").param("localizacao", "bloco a")
                        .header("Authorization", token(Perfil.SOLICITANTE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
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
    void filtraDisponibilidadeDeSalaAntesDepoisEOcupacaoNoIntervaloConflitante() throws Exception {
        salvarReserva(salaOcupada, null, null);

        buscarSalas("2026-11-03T08:00:00-03:00", "2026-11-03T09:00:00-03:00")
                .andExpect(status().isOk()).andExpect(jsonPath("$[?(@.id == " + salaOcupada.getId() + ")]").value(hasSize(1)));
        buscarSalas("2026-11-03T10:00:00-03:00", "2026-11-03T11:00:00-03:00")
                .andExpect(status().isOk()).andExpect(jsonPath("$[?(@.id == " + salaOcupada.getId() + ")]").value(hasSize(1)));
        buscarSalas("2026-11-03T09:30:00-03:00", "2026-11-03T10:30:00-03:00")
                .andExpect(status().isOk()).andExpect(jsonPath("$[?(@.id == " + salaOcupada.getId() + ")]").value(hasSize(0)));
    }

    @Test
    void aplicaDisponibilidadeAProfessorEMaterialEPermiteRecursosSemReservas() throws Exception {
        salvarReserva(null, professorOcupado, null);
        salvarReserva(null, null, materialOcupado);

        mockMvc.perform(get("/api/professores").param("inicio", "2026-11-03T09:00:00-03:00")
                        .param("termino", "2026-11-03T10:00:00-03:00")
                        .header("Authorization", token(Perfil.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + professorOcupado.getId() + ")]").value(hasSize(0)))
                .andExpect(jsonPath("$[?(@.nome == 'Bruno')]").value(hasSize(1)));
        mockMvc.perform(get("/api/materiais").param("inicio", "2026-11-03T09:00:00-03:00")
                        .param("termino", "2026-11-03T10:00:00-03:00")
                        .header("Authorization", token(Perfil.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + materialOcupado.getId() + ")]").value(hasSize(0)))
                .andExpect(jsonPath("$[?(@.nome == 'Projetor B')]").value(hasSize(1)));
        buscarSalas("2026-11-03T09:00:00-03:00", "2026-11-03T10:00:00-03:00")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + salaLivre.getId() + ")]").value(hasSize(1)));
    }

    @Test
    void combinaFiltroTemporalComFiltrosEstruturaisECaseInsensitive() throws Exception {
        salvarReserva(salaOcupada, null, null);
        mockMvc.perform(get("/api/salas").param("tipo", "LABORATORIO")
                        .param("capacidadeMinima", "30").param("localizacao", "BLOCO A")
                        .param("inicio", "2026-11-03T09:00:00-03:00")
                        .param("termino", "2026-11-03T10:00:00-03:00")
                        .header("Authorization", token(Perfil.SOLICITANTE)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Sala livre"));
    }

    @Test
    void rejeitaIntervaloTemporalAusenteParcialIgualOuInvertido() throws Exception {
        mockMvc.perform(get("/api/salas").param("inicio", "2026-11-03T09:00:00-03:00")
                        .header("Authorization", token(Perfil.ADMIN)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/salas").param("inicio", "data-invalida").param("termino", "2026-11-03T10:00:00-03:00")
                        .header("Authorization", token(Perfil.ADMIN)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/salas").param("inicio", "2026-11-03T09:00:00-03:00")
                        .param("termino", "2026-11-03T09:00:00-03:00").header("Authorization", token(Perfil.ADMIN)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/salas").param("inicio", "2026-11-03T10:00:00-03:00")
                        .param("termino", "2026-11-03T09:00:00-03:00").header("Authorization", token(Perfil.ADMIN)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void mantemListagemCompletaQuandoIntervaloNaoInformado() throws Exception {
        buscarSalas(null, null).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void combinaFiltrosDeMaterial() throws Exception {
        mockMvc.perform(get("/api/materiais").param("tipo", "equipamento")
                        .param("localizacao", "bloco a")
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

    private org.springframework.test.web.servlet.ResultActions buscarSalas(String inicio, String termino)
            throws Exception {
        var request = get("/api/salas").header("Authorization", token(Perfil.ADMIN));
        if (inicio != null) request.param("inicio", inicio);
        if (termino != null) request.param("termino", termino);
        return mockMvc.perform(request);
    }

    private void salvarReserva(Sala sala, Professor professor, Material material) {
        reservaRepository.saveAndFlush(new Reserva(solicitante, sala, professor, material,
                OffsetDateTime.parse("2026-11-03T09:00:00-03:00"),
                OffsetDateTime.parse("2026-11-03T10:00:00-03:00"), StatusReserva.SOLICITADA));
    }
}
