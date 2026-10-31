package br.com.senac.organizacaorecursos;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.senac.organizacaorecursos.entity.Material;
import br.com.senac.organizacaorecursos.entity.Perfil;
import br.com.senac.organizacaorecursos.entity.Usuario;
import br.com.senac.organizacaorecursos.repository.MaterialRepository;
import br.com.senac.organizacaorecursos.repository.ProfessorRepository;
import br.com.senac.organizacaorecursos.repository.SalaRepository;
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
class MaterialAuthorizationIntegrationTest {
    private static final String PAYLOAD_VALIDO = """
            {"nome":"Projetor","tipo":"Equipamento","quantidadeTotal":5,
             "quantidadeDisponivel":3,"localizacao":"Almoxarifado","restrito":true,"ativo":true}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @MockBean
    private MaterialRepository materialRepository;

    @MockBean
    private SalaRepository salaRepository;

    @MockBean
    private ProfessorRepository professorRepository;

    @BeforeEach
    void prepararRepositorio() {
        when(materialRepository.save(any(Material.class))).thenAnswer(invocation -> {
            Material material = invocation.getArgument(0);
            ReflectionTestUtils.setField(material, "id", 1L);
            return material;
        });
        when(materialRepository.findAll()).thenReturn(List.of(material(1L)));
        when(materialRepository.findById(1L)).thenReturn(Optional.of(material(1L)));
        when(materialRepository.findById(999L)).thenReturn(Optional.empty());
    }

    @Test
    void adminCadastraMaterialERetornaRestrito() throws Exception {
        mockMvc.perform(post("/api/materiais").header("Authorization", token(Perfil.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content(PAYLOAD_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.restrito").value(true));
    }

    @ParameterizedTest(name = "{0} recebe {2} ao cadastrar material")
    @MethodSource("cenariosCadastro")
    void aplicaAutorizacaoNoCadastro(String descricao, String authorization, int statusEsperado) throws Exception {
        mockMvc.perform(comAutorizacao(post("/api/materiais")
                        .contentType(MediaType.APPLICATION_JSON).content(PAYLOAD_VALIDO), authorization))
                .andExpect(status().is(statusEsperado));
    }

    @ParameterizedTest(name = "{0} recebe {2} ao consultar materiais")
    @MethodSource("cenariosConsulta")
    void permiteConsultaParaTodoPerfilAutenticado(String descricao, String authorization, int statusEsperado)
            throws Exception {
        mockMvc.perform(comAutorizacao(get("/api/materiais"), authorization))
                .andExpect(status().is(statusEsperado));
    }

    @Test
    void consultaMaterialExistentePorId() throws Exception {
        mockMvc.perform(get("/api/materiais/1").header("Authorization", token(Perfil.SOLICITANTE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeDisponivel").value(3));
    }

    @Test
    void retorna404QuandoMaterialNaoExiste() throws Exception {
        mockMvc.perform(get("/api/materiais/999").header("Authorization", token(Perfil.ADMIN)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Material nao encontrado."));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("payloadsInvalidos")
    void rejeitaQuantidadesOuPayloadInvalidos(String descricao, String payload) throws Exception {
        mockMvc.perform(post("/api/materiais").header("Authorization", token(Perfil.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
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

    private static Stream<Arguments> payloadsInvalidos() {
        return Stream.of(
                Arguments.of("campos obrigatorios ausentes", "{\"nome\":\"\",\"tipo\":\"\"}"),
                Arguments.of("quantidade negativa", "{\"nome\":\"Projetor\",\"tipo\":\"Equipamento\",\"quantidadeTotal\":-1,\"quantidadeDisponivel\":0,\"localizacao\":\"A\",\"restrito\":false,\"ativo\":true}"),
                Arguments.of("disponivel maior que total", "{\"nome\":\"Projetor\",\"tipo\":\"Equipamento\",\"quantidadeTotal\":1,\"quantidadeDisponivel\":2,\"localizacao\":\"A\",\"restrito\":false,\"ativo\":true}"));
    }

    private Material material(Long id) {
        Material material = new Material("Projetor", "Equipamento", 5, 3, "Almoxarifado", true, true);
        ReflectionTestUtils.setField(material, "id", id);
        return material;
    }

    private String token(Perfil perfil) {
        return "Bearer " + jwtService.generateToken(new Usuario("Ana", "ana@senac.br", "hash", perfil, true));
    }

    private MockHttpServletRequestBuilder comAutorizacao(MockHttpServletRequestBuilder request, String authorization) {
        return authorization == null ? request : request.header("Authorization", authorization);
    }
}
