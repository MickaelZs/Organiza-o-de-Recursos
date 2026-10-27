package br.com.senac.organizacaorecursos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class OrganizacaoRecursosApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrganizacaoRecursosApplication.class, args);
    }
}
