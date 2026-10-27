package br.com.senac.organizacaorecursos;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.senac.organizacaorecursos.entity.Perfil;
import br.com.senac.organizacaorecursos.entity.Professor;
import br.com.senac.organizacaorecursos.entity.Usuario;
import br.com.senac.organizacaorecursos.repository.ProfessorRepository;
import br.com.senac.organizacaorecursos.repository.UsuarioRepository;
import br.com.senac.organizacaorecursos.security.JwtService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProfessorAuthorizationIntegrationTest {
    private static final String PAYLOAD_VALIDO = """
            {"nome":"Ana Silva","email":"ana.silva@senac.br",
             "competencia":"Matematica","ativo":true}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @MockBean
    private ProfessorRepository professorRepository;

    @BeforeEach
    void prepararRepositorio() {
        when(professorRepository.existsByEmail("ana.silva@senac.br")).thenReturn(false);
        when(professorRepository.save(any(Professor.class))).thenAnswer(invocation -> {
            Professor professor = invocation.getArgument(0);
            ReflectionTestUtils.setField(professor, "id", 1L);
            return professor;
        });
        when(professorRepository.findAll()).thenReturn(List.of(professor(1L)));
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor(1L)));
        when(professorRepository.findById(999L)).thenReturn(Optional.empty());
    }

    @Test
    void adminCadastraProfessorSemCriarUsuario() throws Exception {
        mockMvc.perform(post("/api/professores").header("Authorization", token(Perfil.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content(PAYLOAD_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("ana.silva@senac.br"));

        verifyNoInteractions(usuarioRepository);
    }

    @ParameterizedTest(name = "{0} recebe {2} ao cadastrar professor")
    @MethodSource("cenariosCadastro")
    void aplicaAutorizacaoNoCadastro(String descricao, String authorization, int statusEsperado) throws Exception {
        mockMvc.perform(post("/api/professores").header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON).content(PAYLOAD_VALIDO))
                .andExpect(status().is(statusEsperado));
    }

    @ParameterizedTest(name = "{0} recebe {2} ao consultar professores")
    @MethodSource("cenariosConsulta")
    void permiteConsultaParaTodoPerfilAutenticado(String descricao, String authorization, int statusEsperado)
            throws Exception {
        mockMvc.perform(get("/api/professores").header("Authorization", authorization))
                .andExpect(status().is(statusEsperado));
    }

    @Test
    void consultaProfessorExistentePorId() throws Exception {
        mockMvc.perform(get("/api/professores/1").header("Authorization", token(Perfil.SOLICITANTE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void retorna404QuandoProfessorNaoExiste() throws Exception {
        mockMvc.perform(get("/api/professores/999").header("Authorization", token(Perfil.ADMIN)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Professor nao encontrado."));
    }

    @Test
    void rejeitaPayloadInvalidoParaAdmin() throws Exception {
        mockMvc.perform(post("/api/professores").header("Authorization", token(Perfil.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"email\":\"invalido\",\"competencia\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejeitaEmailDuplicado() throws Exception {
        when(professorRepository.existsByEmail("ana.silva@senac.br")).thenReturn(true);

        mockMvc.perform(post("/api/professores").header("Authorization", token(Perfil.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content(PAYLOAD_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("E-mail de professor ja cadastrado."));
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

    private Professor professor(Long id) {
        Professor professor = new Professor("Ana Silva", "ana.silva@senac.br", "Matematica", true);
        ReflectionTestUtils.setField(professor, "id", id);
        return professor;
    }

    private String token(Perfil perfil) {
        return "Bearer " + jwtService.generateToken(new Usuario("Ana", "ana@senac.br", "hash", perfil, true));
    }
}
