package br.com.senac.organizacaorecursos;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.senac.organizacaorecursos.entity.Perfil;
import br.com.senac.organizacaorecursos.entity.Sala;
import br.com.senac.organizacaorecursos.entity.Usuario;
import br.com.senac.organizacaorecursos.repository.SalaRepository;
import br.com.senac.organizacaorecursos.repository.MaterialRepository;
import br.com.senac.organizacaorecursos.repository.ProfessorRepository;
import br.com.senac.organizacaorecursos.repository.UsuarioRepository;
import br.com.senac.organizacaorecursos.security.JwtService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SalaAuthorizationIntegrationTest {
    private static final String PAYLOAD_VALIDO = """
            {"nome":"Laboratorio 1","tipo":"Laboratorio","capacidade":30,
             "localizacao":"Bloco A","restrita":false,"ativa":true}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @MockBean
    private SalaRepository salaRepository;

    @MockBean
    private ProfessorRepository professorRepository;

    @MockBean
    private MaterialRepository materialRepository;

    @BeforeEach
    void prepararRepositorio() {
        when(salaRepository.save(any(Sala.class))).thenAnswer(invocation -> {
            Sala sala = invocation.getArgument(0);
            ReflectionTestUtils.setField(sala, "id", 1L);
            return sala;
        });
        when(salaRepository.findAll()).thenReturn(List.of(sala(1L)));
        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala(1L)));
        when(salaRepository.findById(999L)).thenReturn(Optional.empty());
    }

    @Test
    void adminCadastraSalaComPayloadValido() throws Exception {
        mockMvc.perform(post("/api/salas").header("Authorization", token(Perfil.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content(PAYLOAD_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Laboratorio 1"));
    }

    @ParameterizedTest(name = "{0} recebe {2} ao cadastrar sala")
    @MethodSource("cenariosCadastro")
    void aplicaAutorizacaoNoCadastro(String descricao, String authorization, int statusEsperado) throws Exception {
        mockMvc.perform(comAutorizacao(post("/api/salas")
                        .contentType(MediaType.APPLICATION_JSON).content(PAYLOAD_VALIDO), authorization))
                .andExpect(status().is(statusEsperado));
    }

    @ParameterizedTest(name = "{0} recebe {2} ao consultar salas")
    @MethodSource("cenariosConsulta")
    void permiteConsultaParaTodoPerfilAutenticado(String descricao, String authorization, int statusEsperado)
            throws Exception {
        mockMvc.perform(comAutorizacao(get("/api/salas"), authorization))
                .andExpect(status().is(statusEsperado));
    }

    @Test
    void consultaSalaExistentePorId() throws Exception {
        mockMvc.perform(get("/api/salas/1").header("Authorization", token(Perfil.SOLICITANTE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void retorna404QuandoSalaNaoExiste() throws Exception {
        mockMvc.perform(get("/api/salas/999").header("Authorization", token(Perfil.ADMIN)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Sala nao encontrada."));
    }

    @Test
    void rejeitaPayloadInvalidoParaAdmin() throws Exception {
        mockMvc.perform(post("/api/salas").header("Authorization", token(Perfil.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"tipo\":\"\",\"capacidade\":0,\"localizacao\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    private Stream<Arguments> cenariosCadastro() {
        return Stream.of(
                Arguments.of("anonimo", null, 401),
                Arguments.of("SOLICITANTE", token(Perfil.SOLICITANTE), 403),
                Arguments.of("RESPONSAVEL", token(Perfil.RESPONSAVEL), 403));
    }

    private Stream<Arguments> cenariosConsulta() {
        return Stream.of(
                Arguments.of("anonimo", null, 401),
                Arguments.of("SOLICITANTE", token(Perfil.SOLICITANTE), 200),
                Arguments.of("RESPONSAVEL", token(Perfil.RESPONSAVEL), 200),
                Arguments.of("ADMIN", token(Perfil.ADMIN), 200));
    }

    private Sala sala(Long id) {
        Sala sala = new Sala("Laboratorio 1", "Laboratorio", 30, "Bloco A", false, true);
        ReflectionTestUtils.setField(sala, "id", id);
        return sala;
    }

    private String token(Perfil perfil) {
        return "Bearer " + jwtService.generateToken(new Usuario("Ana", "ana@senac.br", "hash", perfil, true));
    }

    private MockHttpServletRequestBuilder comAutorizacao(MockHttpServletRequestBuilder request, String authorization) {
        return authorization == null ? request : request.header("Authorization", authorization);
    }
}
