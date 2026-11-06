# Organização de Recursos

Projeto acadêmico da disciplina de Qualidade de Software para gerenciamento de salas, professores, materiais/equipamentos, reservas, manutenção, retirada/devolução, auditoria, notificações e relatórios.

## Tecnologias

- Java 21 e Spring Boot 3.5;
- Spring Web, Data JPA, Validation e Security com JWT;
- PostgreSQL e Flyway;
- JUnit 5, Mockito, Testcontainers/PostgreSQL, WireMock e Cucumber;
- JaCoCo e Springdoc/OpenAPI.

## Pré-requisitos e configuração

- Java 21;
- Docker Desktop em execução para os testes PostgreSQL/Testcontainers;
- PostgreSQL para executar a aplicação fora dos testes.

Crie um banco `organizacao_recursos` e configure, sem versionar segredos:

```text
DB_URL=jdbc:postgresql://localhost:5432/organizacao_recursos
DB_USERNAME=postgres
DB_PASSWORD=sua-senha-local
JWT_SECRET=segredo-fornecido-pelo-ambiente
JWT_EXPIRATION=PT2H
```

`JWT_SECRET` é obrigatório para executar a aplicação e não deve conter um valor real no repositório. `.env.example` serve apenas como referência.

## Executar

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Health check: `GET /api/health`. A especificação OpenAPI fica disponível em `/swagger-ui.html`.
Interface web responsiva: `/`.

## Fluxos e endpoints principais

- Autenticação: `POST /api/auth/login`;
- Recursos: `/api/salas`, `/api/professores` e `/api/materiais`;
- Reservas: `/api/reservas`, aprovação/rejeição e retirada/devolução;
- Manutenção: `/api/manutencoes`;
- Auditoria: `GET /api/auditorias`;
- Notificações: `GET /api/notificacoes` e `PATCH /api/notificacoes/{id}/lida`;
- Relatórios: `GET /api/relatorios/utilizacao`, `/carga-horaria` e `/conflitos-evitados`.

Os relatórios exigem `inicio` e `termino` em ISO-8601 com offset e usam o intervalo semiaberto `[inicio, termino)`. RESPONSAVEL e ADMIN podem consultá-los. A especificação detalhada está em [docs/api-relatorios.md](docs/api-relatorios.md).

## Perfis e segurança

- `SOLICITANTE`: cria/altera/cancela as próprias reservas e consulta as próprias notificações;
- `RESPONSAVEL`: aprova/rejeita reservas restritas, opera manutenção, registra retirada/devolução, consulta auditoria e relatórios;
- `ADMIN`: operações administrativas, retirada/devolução, auditoria e relatórios.

Senhas são armazenadas com BCrypt e não são serializadas. A API é stateless com JWT. Rotas não autenticadas são somente health e login.

## Tratamento de erros

Respostas de erro da API seguem `timestamp`, `status`, `error`, `message` e `path`. Os códigos principais são 400 para entrada inválida, 401 para ausência de autenticação, 403 para falta de permissão, 404 para recurso inexistente e 409 para conflito/regra de estado. Falhas inesperadas retornam 500 sem stack trace. Detalhes estão em [docs/api-erros.md](docs/api-erros.md).

## Migrations

Flyway aplica, em ordem, V1 a V10. V8 cria os campos de retirada/devolução, V9 cria auditorias e V10 cria notificações. RF12, RF13 e RF14 não exigem nova tabela nem V11.

## Testes e qualidade

```bash
./mvnw -B test
./mvnw -B clean verify
```

No Windows, use `./mvnw.cmd -B clean verify`. Os testes críticos de persistência, concorrência e relatórios usam PostgreSQL real por Testcontainers; não há substituição por H2.

Evidência final (05/11/2026): `clean verify` executado com Java 21 terminou em `BUILD SUCCESS`, com 187 testes, 0 falhas, 0 erros e 0 ignorados. O JaCoCo registrou 870/933 linhas cobertas (93,25%) e 259/340 branches cobertos (76,18%), acima das metas de 80% e 70%. Flyway validou e aplicou V1–V10 em PostgreSQL 16.15 limpo, chegando ao schema v10.

## Estrutura

O código fica em `src/main/java/br/com/senac/organizacaorecursos`, organizado em `config`, `controller`, `dto`, `entity`, `exception`, `repository`, `security` e `service`. Requisitos, decisões e rastreabilidade ficam em `docs/`.

Interface responsiva: o sistema disponibiliza uma interface web em `/` com acesso por JWT e painéis condicionados ao perfil. A interface consome as APIs existentes; mensagens de erro são apresentadas sem expor stack traces, hashes ou segredos.
