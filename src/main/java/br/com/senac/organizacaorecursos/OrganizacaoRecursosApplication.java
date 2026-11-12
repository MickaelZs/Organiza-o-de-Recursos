package br.com.senac.organizacaorecursos;

import br.com.senac.organizacaorecursos.cli.AdminBootstrapCommand;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import br.com.senac.organizacaorecursos.config.JwtProperties;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableConfigurationProperties(JwtProperties.class)
public class OrganizacaoRecursosApplication {
    public static void main(String[] args) {
        if (args.length == 1 && "--bootstrap-admin".equals(args[0])) {
            AdminBootstrapCommand.executar();
            return;
        }
        for (String argument : args) {
            if ("--bootstrap-admin".equals(argument)) {
                throw new IllegalArgumentException(
                        "O comando --bootstrap-admin não aceita argumentos adicionais.");
            }
        }
        SpringApplication.run(OrganizacaoRecursosApplication.class, args);
    }
}
