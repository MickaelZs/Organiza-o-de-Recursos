package br.com.senac.organizacaorecursos;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.senac.organizacaorecursos.entity.Perfil;
import br.com.senac.organizacaorecursos.entity.Usuario;
import br.com.senac.organizacaorecursos.repository.AuditoriaRepository;
import br.com.senac.organizacaorecursos.repository.MaterialRepository;
import br.com.senac.organizacaorecursos.repository.NotificacaoRepository;
import br.com.senac.organizacaorecursos.repository.ProfessorRepository;
import br.com.senac.organizacaorecursos.repository.RelatorioRepository;
import br.com.senac.organizacaorecursos.repository.SalaRepository;
import br.com.senac.organizacaorecursos.repository.UsuarioRepository;
import br.com.senac.organizacaorecursos.security.JwtService;
import br.com.senac.organizacaorecursos.service.ReservaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InterfaceErrosIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;

    @MockBean private UsuarioRepository usuarioRepository;
    @MockBean private SalaRepository salaRepository;
    @MockBean private ProfessorRepository professorRepository;
    @MockBean private MaterialRepository materialRepository;
    @MockBean private ReservaService reservaService;
    @MockBean private AuditoriaRepository auditoriaRepository;
    @MockBean private NotificacaoRepository notificacaoRepository;
    @MockBean private RelatorioRepository relatorioRepository;

    @Test
    void padronizaNaoAutenticadoEProtegeDadosSensíveis() throws Exception {
        mockMvc.perform(get("/api/relatorios/utilizacao")
                        .param("inicio", "2026-11-04T09:00:00-03:00")
                        .param("termino", "2026-11-04T10:00:00-03:00"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Autenticacao necessaria."))
                .andExpect(jsonPath("$.path").value("/api/relatorios/utilizacao"))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void padronizaAcessoNegado() throws Exception {
        mockMvc.perform(get("/api/relatorios/utilizacao")
                        .param("inicio", "2026-11-04T09:00:00-03:00")
                        .param("termino", "2026-11-04T10:00:00-03:00")
                        .header("Authorization", token(Perfil.SOLICITANTE)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Acesso negado."))
                .andExpect(jsonPath("$.path").value("/api/relatorios/utilizacao"));
    }

    @Test
    void padronizaRecursoInexistenteEParametroAusente() throws Exception {
        mockMvc.perform(get("/api/inexistente").header("Authorization", token(Perfil.RESPONSAVEL)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/api/inexistente"))
                .andExpect(jsonPath("$.trace").doesNotExist());

        mockMvc.perform(get("/api/relatorios/utilizacao")
                        .header("Authorization", token(Perfil.RESPONSAVEL)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Requisicao invalida."));
    }

    private String token(Perfil perfil) {
        return "Bearer " + jwtService.generateToken(
                new Usuario("Usuario", perfil.name().toLowerCase() + "@senac.br", "hash", perfil, true));
    }
}
