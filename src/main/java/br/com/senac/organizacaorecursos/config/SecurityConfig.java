package br.com.senac.organizacaorecursos.config;

import br.com.senac.organizacaorecursos.exception.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpStatus;
import br.com.senac.organizacaorecursos.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, ObjectMapper objectMapper) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/index.html", "/styles.css", "/app.js",
                                "/swagger-ui.html", "/swagger-ui/**", "/api-docs", "/api-docs/**",
                                "/api/health", "/api/auth/login").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/salas").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/professores").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/materiais").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/manutencoes").hasAnyRole("RESPONSAVEL", "ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/manutencoes/**").hasAnyRole("RESPONSAVEL", "ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/manutencoes/**").hasAnyRole("RESPONSAVEL", "ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.POST,
                                "/api/reservas/*/aprovar", "/api/reservas/*/rejeitar").hasRole("RESPONSAVEL")
                        .requestMatchers(org.springframework.http.HttpMethod.POST,
                                "/api/reservas/*/retirada", "/api/reservas/*/devolucao").hasAnyRole("RESPONSAVEL", "ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/reservas").hasRole("SOLICITANTE")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/reservas/**").hasRole("SOLICITANTE")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/reservas/**").hasRole("SOLICITANTE")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/auditorias", "/api/auditorias/**").hasAnyRole("RESPONSAVEL", "ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/auditoria", "/api/auditoria/**").hasAnyRole("RESPONSAVEL", "ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/notificacoes", "/api/notificacoes/**").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/notificacoes/**").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/relatorios/**")
                                .hasAnyRole("RESPONSAVEL", "ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                escreverErro(response, ApiError.of(HttpStatus.UNAUTHORIZED,
                                        "Autenticacao necessaria.", request)))
                        .accessDeniedHandler((request, response, exception) ->
                                escreverErro(response, ApiError.of(HttpStatus.FORBIDDEN,
                                        "Acesso negado.", request))))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private void escreverErro(HttpServletResponse response, ApiError erro) throws java.io.IOException {
        response.setStatus(erro.status());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), erro);
    }
}
