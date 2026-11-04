# Organização de Recursos

Projeto acadêmico da disciplina de Qualidade de Software. O sistema será destinado ao gerenciamento de salas, professores, materiais/equipamentos e reservas.

**Escopo validado:** RF-01 a RF-07. Autenticação/autorização, recursos, busca estrutural e temporal, reservas, conflitos, proteção contra concorrência e aprovação de recursos restritos estão implementados e validados. RF-08 a RF-14 permanecem fora do escopo concluído.

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

Evidência de integração final (03/11/2026): `clean verify` executado com Java 21.0.11 terminou em BUILD SUCCESS, com 147 testes, 0 falhas, 0 erros e 0 ignorados. O JaCoCo gerou `target/site/jacoco`: 96,37% de linhas (451/468) e 83,87% de branches (156/186), acima das metas de 80% e 70%. Os testes de persistência, conflitos, concorrência e busca usaram PostgreSQL real via Testcontainers 1.21.4; Flyway aplicou V1–V6 em banco limpo, chegando ao schema v6. Testes focados de integração executados antes da suíte: 122 testes, sem falhas ou erros.

## Estrutura resumida

`src/main/java/br/com/senac/organizacaorecursos` contém as camadas `config`, `controller`, `dto`, `entity`, `exception`, `repository`, `security` e `service`. A documentação de qualidade está em `docs/`.

Manutenção, retirada/devolução, auditoria, notificações, relatórios e interface responsiva (RF-08 a RF-14) não pertencem ao escopo implementado.
