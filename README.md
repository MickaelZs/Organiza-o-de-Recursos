# Organização de Recursos

Projeto acadêmico da disciplina de Qualidade de Software. O sistema será destinado ao gerenciamento de salas, professores, materiais/equipamentos e reservas.

**Escopo atual:** RF-01 a RF-03 do Integrante 1. RF-01 e RF-02 estão implementados e validados; RF-03 possui filtros estruturais validados, enquanto a disponibilidade temporal permanece pendente de etapas futuras.

## Tecnologias

Java 21, Spring Boot 3.x, Maven/Maven Wrapper, PostgreSQL, Spring Web, Data JPA, Validation, Security, Thymeleaf, Flyway, OpenAPI/Swagger, JUnit 5, Mockito, JaCoCo, Testcontainers, WireMock e Cucumber JVM.

## Pré-requisitos e configuração

- Java 21
- PostgreSQL para executar a aplicação localmente
- Docker para executar os testes PostgreSQL/Testcontainers

Crie um banco PostgreSQL chamado `organizacao_recursos` e defina as variáveis abaixo (use `.env.example` somente como referência; não versione `.env`):

```text
DB_URL=jdbc:postgresql://localhost:5432/organizacao_recursos
DB_USERNAME=postgres
DB_PASSWORD=sua-senha-local
JWT_SECRET=segredo-base64-fornecido-pelo-ambiente
JWT_EXPIRATION=PT2H
```

## Executar

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Health check técnico: `GET http://localhost:8080/api/health` (retorna `{"status":"UP"}`). A documentação OpenAPI estará em `/swagger-ui.html`.

Autenticação da API: `POST /api/auth/login` recebe `email` e `senha` e retorna um JWT Bearer. O segredo JWT é obrigatório via variável de ambiente e nunca deve ser versionado. Consultas de recursos exigem autenticação; cadastros de Sala, Professor e Material exigem perfil ADMIN.

## Testes e verificação

```bash
./mvnw -B test
./mvnw -B clean verify
```

No Windows, use `mvnw.cmd -B clean verify`. Os testes de persistência e filtros usam PostgreSQL via Testcontainers 1.21.4 e exigem Docker; não há substituição por H2.

Evidência final do `clean verify`: 93 testes executados, 0 falhas, 0 erros, 0 ignorados e BUILD SUCCESS. O JaCoCo gerou `target/site/jacoco` com 97.59% de linhas (243 cobertas / 6 não cobertas) e 76.92% de branches (40 cobertos / 12 não cobertos), acima das metas de 80% e 70%. PostgreSQL real, Testcontainers 1.21.4 e Flyway V1-V5 foram validados em banco limpo.

## Estrutura resumida

`src/main/java/br/com/senac/organizacaorecursos` contém as camadas `config`, `controller`, `dto`, `entity`, `exception`, `repository`, `security` e `service`. A documentação de qualidade está em `docs/`.

Reservas, disponibilidade temporal, manutenção, movimentações, notificações e relatórios não pertencem ao escopo implementado.
