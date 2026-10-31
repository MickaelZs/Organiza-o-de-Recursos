package br.com.senac.organizacaorecursos.security;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.senac.organizacaorecursos.config.JwtProperties;
import br.com.senac.organizacaorecursos.entity.Perfil;
import br.com.senac.organizacaorecursos.entity.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {
    @AfterEach
    void clearContext() { SecurityContextHolder.clearContext(); }

    @Test
    void convertePerfilDoTokenEmAuthorityDoSpringSecurity() throws Exception {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("dGVzdC1qd3Qtc2VjcmV0LXdpdGgtYXQtbGVhc3QtMzItYnl0ZXMtbG9uZw==");
        JwtService jwtService = new JwtService(properties);
        String token = jwtService.generateToken(new Usuario("Ana", "ana@senac.br", "hash", Perfil.RESPONSAVEL, true));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        new JwtAuthenticationFilter(jwtService).doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("ana@senac.br");
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting("authority").containsExactly("ROLE_RESPONSAVEL");
    }

    @Test
    void rejeitaTokenSemPerfilObrigatorio() throws Exception {
        JwtProperties properties = properties();
        String token = io.jsonwebtoken.Jwts.builder().subject("ana@senac.br")
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                        io.jsonwebtoken.io.Decoders.BASE64.decode(properties.getSecret())))
                .compact();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        new JwtAuthenticationFilter(new JwtService(properties)).doFilter(
                request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void rejeitaTokenSemSubjectObrigatorio() throws Exception {
        JwtProperties properties = properties();
        String token = io.jsonwebtoken.Jwts.builder().claim("perfil", "ADMIN")
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                        io.jsonwebtoken.io.Decoders.BASE64.decode(properties.getSecret())))
                .compact();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        new JwtAuthenticationFilter(new JwtService(properties)).doFilter(
                request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    private JwtProperties properties() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("dGVzdC1qd3Qtc2VjcmV0LXdpdGgtYXQtbGVhc3QtMzItYnl0ZXMtbG9uZw==");
        return properties;
    }
}
