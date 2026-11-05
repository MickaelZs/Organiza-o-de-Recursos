package br.com.senac.organizacaorecursos.config;

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

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/health", "/api/auth/login").permitAll()
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
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                response.sendError(HttpStatus.UNAUTHORIZED.value()))
                        .accessDeniedHandler((request, response, exception) ->
                                response.sendError(HttpStatus.FORBIDDEN.value())))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
