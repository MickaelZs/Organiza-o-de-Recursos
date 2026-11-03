package br.com.senac.organizacaorecursos;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.senac.organizacaorecursos.entity.Material;
import br.com.senac.organizacaorecursos.entity.Perfil;
import br.com.senac.organizacaorecursos.entity.Professor;
import br.com.senac.organizacaorecursos.entity.Reserva;
import br.com.senac.organizacaorecursos.entity.Sala;
import br.com.senac.organizacaorecursos.entity.Usuario;
import br.com.senac.organizacaorecursos.repository.MaterialRepository;
import br.com.senac.organizacaorecursos.repository.ProfessorRepository;
import br.com.senac.organizacaorecursos.repository.ReservaRepository;
import br.com.senac.organizacaorecursos.repository.SalaRepository;
import br.com.senac.organizacaorecursos.repository.UsuarioRepository;
import br.com.senac.organizacaorecursos.security.JwtService;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
class ReservaApiPostgresTest {
    private static final String BCRYPT_HASH = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
    private static final String PEDIDO_SALA = """
            {"solicitanteId":999,"salaId":1,"inicio":"2026-11-03T09:00:00-03:00",
             "termino":"2026-11-03T10:00:00-03:00"}
            """;

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
    private Usuario outroSolicitante;
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
        solicitante = salvarUsuario("ana@senac.br", Perfil.SOLICITANTE);
        outroSolicitante = salvarUsuario("bia@senac.br", Perfil.SOLICITANTE);
        responsavel = salvarUsuario("responsavel@senac.br", Perfil.RESPONSAVEL);
        admin = salvarUsuario("admin@senac.br", Perfil.ADMIN);
        sala = salaRepository.saveAndFlush(new Sala("Sala 1", "Aula", 30, "Bloco A", false, true));
        professor = professorRepository.saveAndFlush(
                new Professor("Carlos Souza", "carlos@senac.br", "Matematica", true));
        material = materialRepository.saveAndFlush(
                new Material("Projetor", "Equipamento", 4, 4, "Almoxarifado", false, true));
    }

    @Test
    void criaReservaValidaComSolicitanteDoJwtEStatusSolicitada() throws Exception {
        mockMvc.perform(post("/api/reservas").header("Authorization", token(solicitante))
                        .contentType(MediaType.APPLICATION_JSON).content(PEDIDO_SALA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.solicitanteId").value(solicitante.getId()))
                .andExpect(jsonPath("$.salaId").value(sala.getId()))
                .andExpect(jsonPath("$.professorId").doesNotExist())
                .andExpect(jsonPath("$.status").value("SOLICITADA"));
    }

    @Test
    void rejeitaCriacaoAnonimaEPorPerfisSemPapelDeSolicitante() throws Exception {
        mockMvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON).content(PEDIDO_SALA))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/reservas").header("Authorization", token(responsavel))
                        .contentType(MediaType.APPLICATION_JSON).content(PEDIDO_SALA))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/reservas").header("Authorization", token(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(PEDIDO_SALA))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejeitaPayloadInvalidoCom400() throws Exception {
        mockMvc.perform(post("/api/reservas").header("Authorization", token(solicitante))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"salaId\":1,\"professorId\":1,\"inicio\":null,\"termino\":null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criaReservaParaProfessorOuMaterial() throws Exception {
        mockMvc.perform(post("/api/reservas").header("Authorization", token(solicitante))
                        .contentType(MediaType.APPLICATION_JSON).content(pedidoProfessor()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.professorId").value(professor.getId()))
                .andExpect(jsonPath("$.salaId").doesNotExist());
        mockMvc.perform(post("/api/reservas").header("Authorization", token(solicitante))
                        .contentType(MediaType.APPLICATION_JSON).content(pedidoMaterial()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.materialId").value(material.getId()))
                .andExpect(jsonPath("$.salaId").doesNotExist());
    }

    @Test
    void retorna404ParaRecursoOuReservaInexistente() throws Exception {
        mockMvc.perform(post("/api/reservas").header("Authorization", token(solicitante))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"salaId\":999,\"inicio\":\"2026-11-03T09:00:00-03:00\","
                                + "\"termino\":\"2026-11-03T10:00:00-03:00\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/reservas/999").header("Authorization", token(solicitante)))
                .andExpect(status().isNotFound());
    }

    @Test
    void requisitanteListaSomenteAsPropriasEEquipeConsultaTodas() throws Exception {
        salvarReserva(solicitante, sala, null, null);
        salvarReserva(outroSolicitante, null, professor, null);

        mockMvc.perform(get("/api/reservas")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/reservas").header("Authorization", token(solicitante)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].solicitanteId").value(solicitante.getId()))
                .andExpect(jsonPath("$[1]").doesNotExist());
        mockMvc.perform(get("/api/reservas").header("Authorization", token(responsavel)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[1]").exists());
        mockMvc.perform(get("/api/reservas").header("Authorization", token(admin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[1]").exists());
    }

    @Test
    void consultaReservaPropriaEDevolve403ParaReservaAlheia() throws Exception {
        Reserva propria = salvarReserva(solicitante, sala, null, null);
        Reserva alheia = salvarReserva(outroSolicitante, null, professor, null);

        mockMvc.perform(get("/api/reservas/" + propria.getId()).header("Authorization", token(solicitante)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(propria.getId()));
        mockMvc.perform(get("/api/reservas/" + alheia.getId()).header("Authorization", token(solicitante)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/reservas/" + alheia.getId()).header("Authorization", token(responsavel)))
                .andExpect(status().isOk());
    }

    @Test
    void atualizaReservaPropriaERejeitaAlteracaoDeReservaAlheia() throws Exception {
        Reserva propria = salvarReserva(solicitante, sala, null, null);
        Reserva alheia = salvarReserva(outroSolicitante, null, professor, null);

        mockMvc.perform(put("/api/reservas/" + propria.getId()).header("Authorization", token(solicitante))
                        .contentType(MediaType.APPLICATION_JSON).content(pedidoProfessor()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.professorId").value(professor.getId()))
                .andExpect(jsonPath("$.salaId").doesNotExist());
        mockMvc.perform(put("/api/reservas/" + alheia.getId()).header("Authorization", token(solicitante))
                        .contentType(MediaType.APPLICATION_JSON).content(PEDIDO_SALA))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/reservas/" + propria.getId()).header("Authorization", token(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(PEDIDO_SALA))
                .andExpect(status().isForbidden());
    }

    @Test
    void retorna400ParaAtualizacaoComIntervaloInvalido() throws Exception {
        Reserva reserva = salvarReserva(solicitante, sala, null, null);
        mockMvc.perform(put("/api/reservas/" + reserva.getId()).header("Authorization", token(solicitante))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"materialId\":1,\"inicio\":\"2026-11-03T10:00:00-03:00\","
                                + "\"termino\":\"2026-11-03T09:00:00-03:00\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelaReservaPropriaSemApagaLaERejeitaCancelamentoAlheio() throws Exception {
        Reserva propria = salvarReserva(solicitante, sala, null, null);
        Reserva alheia = salvarReserva(outroSolicitante, null, null, material);

        mockMvc.perform(delete("/api/reservas/" + propria.getId()).header("Authorization", token(solicitante)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/reservas/" + propria.getId()).header("Authorization", token(admin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELADA"));
        mockMvc.perform(delete("/api/reservas/" + alheia.getId()).header("Authorization", token(solicitante)))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/reservas/999").header("Authorization", token(solicitante)))
                .andExpect(status().isNotFound());
    }

    private Usuario salvarUsuario(String email, Perfil perfil) {
        return usuarioRepository.saveAndFlush(new Usuario(email, email, BCRYPT_HASH, perfil, true));
    }

    private Reserva salvarReserva(Usuario dono, Sala reservaSala, Professor reservaProfessor, Material reservaMaterial) {
        return reservaRepository.saveAndFlush(new Reserva(dono, reservaSala, reservaProfessor, reservaMaterial,
                OffsetDateTime.parse("2026-11-03T09:00:00-03:00"),
                OffsetDateTime.parse("2026-11-03T10:00:00-03:00"),
                br.com.senac.organizacaorecursos.entity.StatusReserva.SOLICITADA));
    }

    private String token(Usuario usuario) { return "Bearer " + jwtService.generateToken(usuario); }

    private String pedidoProfessor() {
        return "{\"professorId\":" + professor.getId()
                + ",\"inicio\":\"2026-11-03T11:00:00-03:00\","
                + "\"termino\":\"2026-11-03T12:00:00-03:00\"}";
    }

    private String pedidoMaterial() {
        return "{\"materialId\":" + material.getId()
                + ",\"inicio\":\"2026-11-03T13:00:00-03:00\","
                + "\"termino\":\"2026-11-03T14:00:00-03:00\"}";
    }
}
