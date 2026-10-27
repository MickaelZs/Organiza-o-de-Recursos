package br.com.senac.organizacaorecursos.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.senac.organizacaorecursos.service.AuthService;
import br.com.senac.organizacaorecursos.service.InvalidCredentialsException;
import br.com.senac.organizacaorecursos.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @Test
    void retornaTokenNoLoginValido() throws Exception {
        when(authService.login("ana@senac.br", "senha-correta")).thenReturn("jwt-valido");

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@senac.br\",\"senha\":\"senha-correta\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-valido"))
                .andExpect(jsonPath("$.tipo").value("Bearer"));
    }

    @Test
    void retornaMensagemGenericaParaSenhaIncorreta() throws Exception {
        when(authService.login("ana@senac.br", "senha-errada")).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@senac.br\",\"senha\":\"senha-errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciais inválidas."));
    }

    @Test
    void retornaMesmaRespostaParaUsuarioInexistente() throws Exception {
        when(authService.login("ausente@senac.br", "qualquer-senha")).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ausente@senac.br\",\"senha\":\"qualquer-senha\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciais inválidas."));
    }
}
