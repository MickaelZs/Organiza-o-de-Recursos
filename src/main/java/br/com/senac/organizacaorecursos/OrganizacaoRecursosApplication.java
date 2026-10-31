package br.com.senac.organizacaorecursos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import br.com.senac.organizacaorecursos.config.JwtProperties;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableConfigurationProperties(JwtProperties.class)
public class OrganizacaoRecursosApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrganizacaoRecursosApplication.class, args);
    }
}
