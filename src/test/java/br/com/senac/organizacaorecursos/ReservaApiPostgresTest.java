package br.com.senac.organizacaorecursos;

import static org.assertj.core.api.Assertions.assertThat;
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
import br.com.senac.organizacaorecursos.entity.StatusReserva;
import br.com.senac.organizacaorecursos.entity.Usuario;
import br.com.senac.organizacaorecursos.repository.AuditoriaRepository;
import br.com.senac.organizacaorecursos.repository.MaterialRepository;
import br.com.senac.organizacaorecursos.repository.NotificacaoRepository;
import br.com.senac.organizacaorecursos.repository.ProfessorRepository;
import br.com.senac.organizacaorecursos.repository.ReservaRepository;
import br.com.senac.organizacaorecursos.repository.SalaRepository;
import br.com.senac.organizacaorecursos.repository.UsuarioRepository;
import br.com.senac.organizacaorecursos.security.JwtService;
import java.time.OffsetDateTime;
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
import java.util.stream.Stream;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("postgres-test")
@Testcontainers
class ReservaApiPostgresTest {
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
    @Autowired private NotificacaoRepository notificacaoRepository;
    @Autowired private AuditoriaRepository auditoriaRepository;

    private Usuario solicitante;
    private Usuario outroSolicitante;
    private Usuario responsavel;
    private Usuario admin;
    private Sala sala;
    private Sala salaRestrita;
    private Professor professor;
    private Material material;
    private Material materialRestrito;

    @BeforeEach
    void preparaBanco() {
        notificacaoRepository.deleteAll();
        auditoriaRepository.deleteAll();
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
        salaRestrita = salaRepository.saveAndFlush(
                new Sala("Sala restrita", "Restrita", 20, "Bloco B", true, true));
        professor = professorRepository.saveAndFlush(
                new Professor("Carlos Souza", "carlos@senac.br", "Matematica", true));
        material = materialRepository.saveAndFlush(
                new Material("Projetor", "Equipamento", 4, 4, "Almoxarifado", false, true));
        materialRestrito = materialRepository.saveAndFlush(
                new Material("Camera restrita", "Restrito", 1, 1, "Sala do Responsavel", true, true));
    }

    @Test
    void criaReservaComumJaAprovadaComSolicitanteDoJwt() throws Exception {
        mockMvc.perform(post("/api/reservas").header("Authorization", token(solicitante))
                        .contentType(MediaType.APPLICATION_JSON).content(pedidoSala()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.solicitanteId").value(solicitante.getId()))
                .andExpect(jsonPath("$.salaId").value(sala.getId()))
                .andExpect(jsonPath("$.professorId").doesNotExist())
                .andExpect(jsonPath("$.status").value("APROVADA"));
    }

    @Test
    void registraNaoComparecimentoComAuditoriaNotificacaoEPersistencia() throws Exception {
        Reserva reserva = criarReservaComum();

        mockMvc.perform(post("/api/reservas/" + reserva.getId() + "/nao-compareceu")
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NAO_COMPARECEU"));

        assertThat(reservaRepository.findById(reserva.getId()).orElseThrow().getStatus())
                .isEqualTo(StatusReserva.NAO_COMPARECEU);
        assertThat(auditoriaRepository.findAllByOrderByCriadoEmDesc())
                .filteredOn(auditoria -> auditoria.getEntidadeId().equals(reserva.getId()))
                .anySatisfy(auditoria -> {
                    assertThat(auditoria.getAcao()).isEqualTo("NAO_COMPARECEU");
                    assertThat(auditoria.getEstadoAnterior()).isEqualTo("APROVADA");
                    assertThat(auditoria.getEstadoPosterior()).isEqualTo("NAO_COMPARECEU");
                    assertThat(auditoria.getDetalhes()).contains("\"operacao\":\"NAO_COMPARECEU\"")
                            .contains("\"status\":\"APROVADA\"")
                            .contains("\"status\":\"NAO_COMPARECEU\"")
                            .doesNotContain("senha", "token", "jwt");
                });
        assertThat(notificacaoRepository.findAllByOrderByCriadaEmDesc())
                .filteredOn(notificacao -> notificacao.getEntidadeId().equals(reserva.getId()))
                .anySatisfy(notificacao -> assertThat(notificacao.getTipo()).isEqualTo("RESERVA_NAO_COMPARECEU"));
    }

    @Test
    void naoComparecimentoExigePerfilOperacionalEProtegeReservaDeTerceiro() throws Exception {
        Reserva propria = criarReservaComum();
        Reserva alheia = salvarReserva(outroSolicitante, null, professor, null, StatusReserva.APROVADA);

        mockMvc.perform(post("/api/reservas/" + propria.getId() + "/nao-compareceu"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/reservas/" + propria.getId() + "/nao-compareceu")
                        .header("Authorization", token(solicitante)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/reservas/" + alheia.getId() + "/nao-compareceu")
                        .header("Authorization", token(outroSolicitante)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/reservas/" + alheia.getId() + "/nao-compareceu")
                        .header("Authorization", token(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NAO_COMPARECEU"));
    }

    @Test
    void naoComparecimentoRetorna404ParaReservaInexistente() throws Exception {
        mockMvc.perform(post("/api/reservas/999999/nao-compareceu")
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @MethodSource("statusQueNaoPermitemNaoComparecimento")
    void naoComparecimentoRejeitaEstadosIncompativeisSemCriarAuditoria(StatusReserva status) throws Exception {
        Reserva reserva = salvarReserva(solicitante, sala, null, null, status);

        mockMvc.perform(post("/api/reservas/" + reserva.getId() + "/nao-compareceu")
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isConflict());

        assertThat(reservaRepository.findById(reserva.getId()).orElseThrow().getStatus()).isEqualTo(status);
        assertThat(auditoriaRepository.findAllByOrderByCriadoEmDesc())
                .filteredOn(auditoria -> auditoria.getEntidadeId().equals(reserva.getId())
                        && "NAO_COMPARECEU".equals(auditoria.getAcao()))
                .isEmpty();
    }

    @Test
    void naoComparecimentoRepetidoNaoDuplicaHistoricoNemNotificacao() throws Exception {
        Reserva reserva = criarReservaComum();

        mockMvc.perform(post("/api/reservas/" + reserva.getId() + "/nao-compareceu")
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/reservas/" + reserva.getId() + "/nao-compareceu")
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isConflict());

        assertThat(auditoriaRepository.findAllByOrderByCriadoEmDesc())
                .filteredOn(auditoria -> auditoria.getEntidadeId().equals(reserva.getId())
                        && "NAO_COMPARECEU".equals(auditoria.getAcao()))
                .hasSize(1);
        assertThat(notificacaoRepository.findAllByOrderByCriadaEmDesc())
                .filteredOn(notificacao -> notificacao.getEntidadeId().equals(reserva.getId())
                        && "RESERVA_NAO_COMPARECEU".equals(notificacao.getTipo()))
                .hasSize(1);
    }

    @Test
    void rejeitaCriacaoAnonimaEPorPerfisSemPapelDeSolicitante() throws Exception {
        mockMvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON).content(pedidoSala()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/reservas").header("Authorization", token(responsavel))
                        .contentType(MediaType.APPLICATION_JSON).content(pedidoSala()))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/reservas").header("Authorization", token(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(pedidoSala()))
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
                .andExpect(jsonPath("$.salaId").doesNotExist())
                .andExpect(jsonPath("$.status").value("APROVADA"));
    }

    @Test
    void solicitaAprovacaoParaSalaEMaterialRestritos() throws Exception {
        mockMvc.perform(post("/api/reservas").header("Authorization", token(solicitante))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedido("salaId", salaRestrita.getId(), "2026-11-03T09:00:00-03:00",
                                "2026-11-03T10:00:00-03:00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SOLICITADA"));
        mockMvc.perform(post("/api/reservas").header("Authorization", token(solicitante))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedido("materialId", materialRestrito.getId(), "2026-11-03T09:00:00-03:00",
                                "2026-11-03T10:00:00-03:00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SOLICITADA"));
    }

    @Test
    void somenteResponsavelPodeAprovarSolicitacaoRestrita() throws Exception {
        Reserva pendente = criarReservaRestrita();

        mockMvc.perform(post("/api/reservas/" + pendente.getId() + "/aprovar")
                        .header("Authorization", token(solicitante)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/reservas/" + pendente.getId() + "/aprovar")
                        .header("Authorization", token(admin)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/reservas/" + pendente.getId() + "/rejeitar")
                        .header("Authorization", token(solicitante)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/reservas/" + pendente.getId() + "/rejeitar")
                        .header("Authorization", token(admin)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/reservas/" + pendente.getId() + "/aprovar")
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADA"));

        var respostaMaterial = mockMvc.perform(post("/api/reservas").header("Authorization", token(solicitante))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedido("materialId", materialRestrito.getId(), "2026-11-03T09:00:00-03:00",
                                "2026-11-03T10:00:00-03:00")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("SOLICITADA"))
                .andReturn();
        Number idMaterial = com.jayway.jsonpath.JsonPath.read(
                respostaMaterial.getResponse().getContentAsString(), "$.id");
        mockMvc.perform(post("/api/reservas/" + idMaterial.longValue() + "/aprovar")
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materialId").value(materialRestrito.getId()))
                .andExpect(jsonPath("$.status").value("APROVADA"));
    }

    @Test
    void rejeicaoDeSolicitacaoRestritaLiberaConflitoEDisponibilidade() throws Exception {
        Reserva pendente = criarReservaRestrita();
        mockMvc.perform(get("/api/salas").param("tipo", "restrita")
                        .param("inicio", "2026-11-03T09:00:00-03:00")
                        .param("termino", "2026-11-03T10:00:00-03:00")
                        .header("Authorization", token(solicitante)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(post("/api/reservas").header("Authorization", token(solicitante))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedido("salaId", salaRestrita.getId(), "2026-11-03T09:00:00-03:00",
                                "2026-11-03T10:00:00-03:00")))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/reservas/" + pendente.getId() + "/rejeitar")
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJEITADA"));
        mockMvc.perform(get("/api/salas").param("tipo", "restrita")
                        .param("inicio", "2026-11-03T09:00:00-03:00")
                        .param("termino", "2026-11-03T10:00:00-03:00")
                        .header("Authorization", token(solicitante)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(post("/api/reservas").header("Authorization", token(solicitante))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedido("salaId", salaRestrita.getId(), "2026-11-03T09:00:00-03:00",
                                "2026-11-03T10:00:00-03:00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SOLICITADA"));
    }

    @Test
    void retorna404ParaAprovacaoOuRejeicaoInexistente() throws Exception {
        mockMvc.perform(post("/api/reservas/999/aprovar").header("Authorization", token(responsavel)))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/reservas/999/rejeitar").header("Authorization", token(responsavel)))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejeitaTransicoesDeAprovacaoInvalidas() throws Exception {
        Reserva comum = criarReservaComum();
        mockMvc.perform(post("/api/reservas/" + comum.getId() + "/aprovar")
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/reservas/" + comum.getId() + "/rejeitar")
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isConflict());

        Reserva rejeitada = criarReservaRestrita();
        mockMvc.perform(post("/api/reservas/" + rejeitada.getId() + "/rejeitar")
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/reservas/" + rejeitada.getId() + "/aprovar")
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isConflict());
    }

    @Test
    void aprovacaoRevalidaConflitoAntesDeConfirmar() throws Exception {
        Reserva pendente = criarReservaRestrita();
        reservaRepository.saveAndFlush(new Reserva(solicitante, salaRestrita, null, null,
                OffsetDateTime.parse("2026-11-03T09:30:00-03:00"),
                OffsetDateTime.parse("2026-11-03T10:30:00-03:00"), StatusReserva.APROVADA));

        mockMvc.perform(post("/api/reservas/" + pendente.getId() + "/aprovar")
                        .header("Authorization", token(responsavel)))
                .andExpect(status().isConflict());
        mockMvc.perform(get("/api/reservas/" + pendente.getId()).header("Authorization", token(responsavel)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SOLICITADA"));
    }

    @Test
    void alterarReservaReavaliaSeORecursoExigeAprovacao() throws Exception {
        Reserva comum = criarReservaComum();
        mockMvc.perform(put("/api/reservas/" + comum.getId()).header("Authorization", token(solicitante))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedido("salaId", salaRestrita.getId(), "2026-11-03T09:00:00-03:00",
                                "2026-11-03T10:00:00-03:00")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SOLICITADA"));
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

    @ParameterizedTest
    @MethodSource("intervalosSobrepostos")
    void rejeitaSobreposicaoDeSalaCom409(String novoInicio, String novoTermino) throws Exception {
        salvarReserva(solicitante, sala, null, null);

        mockMvc.perform(post("/api/reservas").header("Authorization", token(solicitante))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedido("salaId", sala.getId(), novoInicio, novoTermino)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Horario indisponivel para o recurso."));
    }

    @Test
    void permiteIntervalosAdjacentes() throws Exception {
        salvarReserva(solicitante, sala, null, null);

        mockMvc.perform(post("/api/reservas").header("Authorization", token(solicitante))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedido("salaId", sala.getId(), "2026-11-03T10:00:00-03:00",
                                "2026-11-03T11:00:00-03:00")))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/reservas").header("Authorization", token(solicitante))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedido("salaId", sala.getId(), "2026-11-03T08:00:00-03:00",
                                "2026-11-03T09:00:00-03:00")))
                .andExpect(status().isCreated());
    }

    @Test
    void detectaConflitosParaProfessorEMaterial() throws Exception {
        salvarReserva(solicitante, null, professor, null);
        salvarReserva(outroSolicitante, null, null, material);

        mockMvc.perform(post("/api/reservas").header("Authorization", token(solicitante))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedido("professorId", professor.getId(), "2026-11-03T09:30:00-03:00",
                                "2026-11-03T10:30:00-03:00")))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/reservas").header("Authorization", token(solicitante))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedido("materialId", material.getId(), "2026-11-03T09:30:00-03:00",
                                "2026-11-03T10:30:00-03:00")))
                .andExpect(status().isConflict());
    }

    @Test
    void naoBloqueiaMesmoHorarioEmRecursosDiferentes() throws Exception {
        salvarReserva(solicitante, sala, null, null);

        mockMvc.perform(post("/api/reservas").header("Authorization", token(solicitante))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedido("professorId", professor.getId(), "2026-11-03T09:00:00-03:00",
                                "2026-11-03T10:00:00-03:00")))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/reservas").header("Authorization", token(solicitante))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedido("materialId", material.getId(), "2026-11-03T09:00:00-03:00",
                                "2026-11-03T10:00:00-03:00")))
                .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @MethodSource("statusesQueBloqueiam")
    void statusAtivosBloqueiamHorario(StatusReserva status) throws Exception {
        salvarReserva(solicitante, sala, null, null, status);

        mockMvc.perform(post("/api/reservas").header("Authorization", token(outroSolicitante))
                        .contentType(MediaType.APPLICATION_JSON).content(pedidoSala()))
                .andExpect(status().isConflict());
    }

    @ParameterizedTest
    @MethodSource("statusesQueNaoBloqueiam")
    void statusAlternativosNaoBloqueiamHorario(StatusReserva status) throws Exception {
        salvarReserva(solicitante, sala, null, null, status);

        mockMvc.perform(post("/api/reservas").header("Authorization", token(outroSolicitante))
                        .contentType(MediaType.APPLICATION_JSON).content(pedidoSala()))
                .andExpect(status().isCreated());
    }

    @Test
    void atualizarExcluiAPropriaReservaDaVerificacaoMasRejeitaConflitoComOutra() throws Exception {
        Reserva propria = salvarReserva(solicitante, sala, null, null);
        salvarReserva(outroSolicitante, sala, null, null, StatusReserva.SOLICITADA,
                OffsetDateTime.parse("2026-11-03T10:00:00-03:00"),
                OffsetDateTime.parse("2026-11-03T11:00:00-03:00"));

        mockMvc.perform(put("/api/reservas/" + propria.getId()).header("Authorization", token(solicitante))
                        .contentType(MediaType.APPLICATION_JSON).content(pedidoSala()))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/reservas/" + propria.getId()).header("Authorization", token(solicitante))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedido("salaId", sala.getId(), "2026-11-03T10:30:00-03:00",
                                "2026-11-03T11:30:00-03:00")))
                .andExpect(status().isConflict());
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
                        .contentType(MediaType.APPLICATION_JSON).content(pedidoSala()))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/reservas/" + propria.getId()).header("Authorization", token(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(pedidoSala()))
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

    @ParameterizedTest
    @MethodSource("statusesQueNaoPodemSerCancelados")
    void naoPermiteCancelarReservaJaIniciadaOuConcluida(StatusReserva status) throws Exception {
        Reserva reserva = salvarReserva(solicitante, sala, null, null, status);

        mockMvc.perform(delete("/api/reservas/" + reserva.getId())
                        .header("Authorization", token(solicitante)))
                .andExpect(status().isConflict());
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
        return salvarReserva(dono, reservaSala, reservaProfessor, reservaMaterial, StatusReserva.SOLICITADA);
    }

    private Reserva criarReservaRestrita() {
        return reservaRepository.saveAndFlush(new Reserva(solicitante, salaRestrita, null, null,
                OffsetDateTime.parse("2026-11-03T09:00:00-03:00"),
                OffsetDateTime.parse("2026-11-03T10:00:00-03:00"), StatusReserva.SOLICITADA));
    }

    private Reserva criarReservaComum() {
        return reservaRepository.saveAndFlush(new Reserva(solicitante, sala, null, null,
                OffsetDateTime.parse("2026-11-03T09:00:00-03:00"),
                OffsetDateTime.parse("2026-11-03T10:00:00-03:00"), StatusReserva.APROVADA));
    }

    private Reserva salvarReserva(Usuario dono, Sala reservaSala, Professor reservaProfessor, Material reservaMaterial,
                                  StatusReserva status) {
        return salvarReserva(dono, reservaSala, reservaProfessor, reservaMaterial, status,
                OffsetDateTime.parse("2026-11-03T09:00:00-03:00"),
                OffsetDateTime.parse("2026-11-03T10:00:00-03:00"));
    }

    private Reserva salvarReserva(Usuario dono, Sala reservaSala, Professor reservaProfessor, Material reservaMaterial,
                                  StatusReserva status, OffsetDateTime inicio, OffsetDateTime termino) {
        return reservaRepository.saveAndFlush(new Reserva(dono, reservaSala, reservaProfessor, reservaMaterial,
                inicio, termino, status));
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

    private String pedido(String campoRecurso, Long recursoId, String inicio, String termino) {
        return "{\"" + campoRecurso + "\":" + recursoId + ",\"inicio\":\"" + inicio
                + "\",\"termino\":\"" + termino + "\"}";
    }

    private String pedidoSala() {
        return pedido("salaId", sala.getId(), "2026-11-03T09:00:00-03:00", "2026-11-03T10:00:00-03:00");
    }

    private static Stream<Arguments> intervalosSobrepostos() {
        return Stream.of(
                Arguments.of("2026-11-03T09:30:00-03:00", "2026-11-03T10:30:00-03:00"),
                Arguments.of("2026-11-03T08:30:00-03:00", "2026-11-03T09:30:00-03:00"),
                Arguments.of("2026-11-03T09:15:00-03:00", "2026-11-03T09:45:00-03:00"),
                Arguments.of("2026-11-03T08:00:00-03:00", "2026-11-03T11:00:00-03:00"),
                Arguments.of("2026-11-03T09:00:00-03:00", "2026-11-03T10:00:00-03:00"));
    }

    private static Stream<Arguments> statusesQueBloqueiam() {
        return Stream.of(StatusReserva.SOLICITADA, StatusReserva.APROVADA,
                        StatusReserva.EM_USO, StatusReserva.CONCLUIDA)
                .map(Arguments::of);
    }

    private static Stream<Arguments> statusesQueNaoBloqueiam() {
        return Stream.of(StatusReserva.REJEITADA, StatusReserva.CANCELADA, StatusReserva.NAO_COMPARECEU)
                .map(Arguments::of);
    }

    private static Stream<Arguments> statusesQueNaoPodemSerCancelados() {
        return Stream.of(StatusReserva.EM_USO, StatusReserva.CONCLUIDA).map(Arguments::of);
    }

    private static Stream<Arguments> statusQueNaoPermitemNaoComparecimento() {
        return Stream.of(StatusReserva.SOLICITADA, StatusReserva.EM_USO, StatusReserva.CONCLUIDA,
                        StatusReserva.REJEITADA, StatusReserva.CANCELADA, StatusReserva.NAO_COMPARECEU)
                .map(Arguments::of);
    }
}
