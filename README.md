# Organização de Recursos

Projeto acadêmico da disciplina de Qualidade de Software. O sistema será destinado ao gerenciamento de salas, professores, materiais/equipamentos e reservas.

**Fase atual: Fase 0 — Fundação**  
**Requisitos funcionais implementados: 0/14**

## Tecnologias

Java 21, Spring Boot 3.x, Maven/Maven Wrapper, PostgreSQL, Spring Web, Data JPA, Validation, Security, Thymeleaf, Flyway, OpenAPI/Swagger, JUnit 5, Mockito, JaCoCo, Testcontainers, WireMock e Cucumber JVM.

## Pré-requisitos e configuração

- Java 21
- PostgreSQL para executar a aplicação localmente

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

Autenticação da API: `POST /api/auth/login` recebe `email` e `senha` e retorna um JWT Bearer. O segredo JWT é obrigatório via variável de ambiente e nunca deve ser versionado.

## Testes e verificação

```bash
./mvnw -B test
./mvnw -B verify
```

No Windows, use `mvnw.cmd -B verify`. O perfil `test` inicia o contexto sem depender de PostgreSQL local; testes futuros que exigirem comportamento real do banco devem usar Testcontainers PostgreSQL, não H2.

JaCoCo gera o relatório em `target/site/jacoco`. As metas finais (linhas >= 80%, branches >= 70%) serão ativadas progressivamente quando houver regras de negócio testáveis; nesta fundação elas ainda não bloqueiam o build.

## Estrutura resumida

`src/main/java/br/com/senac/organizacaorecursos` contém as camadas preparadas (`config`, `controller`, `dto`, `entity`, `exception`, `repository`, `security`, `service`, `integration`). A documentação de qualidade está em `docs/`.

Nesta fase não há autenticação, cadastros, reservas ou demais requisitos funcionais.
