# Matriz de autorizacao

## Matriz implementada

Os endpoints abaixo existem nesta etapa. A configuracao real do Spring Security e exercitada por testes de integracao com `MockMvc`.

| Operacao | Anonimo | SOLICITANTE | RESPONSAVEL | ADMIN | Evidencia |
|---|---|---|---|---|---|
| `GET /api/health` | 200 | 200 | 200 | 200 | `permiteOperacoesPublicasSemToken` (health sem token) |
| `POST /api/auth/login` | Endpoint acessivel; 2xx com credencial valida / 400 com payload invalido | Mesmo comportamento | Mesmo comportamento | Mesmo comportamento | `permiteOperacoesPublicasSemToken` e `AuthControllerTest` |
| `GET /api/salas` | 401 | 200 | 200 | 200 | `SalaAuthorizationIntegrationTest`: 11 testes executados, 0 falhas/erros |
| `GET /api/salas/{id}` | 401 | 200/404 | 200/404 | 200/404 | `SalaAuthorizationIntegrationTest`: executado |
| `POST /api/salas` | 401 | 403 | 403 | 201 com payload valido; 400 com payload invalido | `SalaAuthorizationIntegrationTest`: executado |
| `GET /api/professores` | 401 | 200 | 200 | 200 | `ProfessorAuthorizationIntegrationTest`: 12 testes executados, 0 falhas/erros |
| `GET /api/professores/{id}` | 401 | 200/404 | 200/404 | 200/404 | `ProfessorAuthorizationIntegrationTest`: executado |
| `POST /api/professores` | 401 | 403 | 403 | 201 com payload valido; 400 com payload invalido; 409 para e-mail duplicado | `ProfessorAuthorizationIntegrationTest`: executado |
| `GET /api/materiais` | 401 | 200 | 200 | 200 | `MaterialAuthorizationIntegrationTest`: 13 testes executados, 0 falhas/erros |
| `GET /api/salas` com filtros `tipo`, `capacidadeMinima`, `localizacao` | 401 | 200 | 200 | 200 | `ResourceSearchApiPostgresTest`: 19 testes executados na suite final, 0 falhas/erros |
| `GET /api/professores` com filtro `competencia` | 401 | 200 | 200 | 200 | `ResourceSearchApiPostgresTest`: executado na suite final |
| `GET /api/materiais` com filtros `tipo`, `localizacao` | 401 | 200 | 200 | 200 | `ResourceSearchApiPostgresTest`: executado na suite final |
| `GET /api/materiais/{id}` | 401 | 200/404 | 200/404 | 200/404 | `MaterialAuthorizationIntegrationTest`: executado |
| `POST /api/materiais` | 401 | 403 | 403 | 201 com payload valido; 400 para quantidade/payload invalido | `MaterialAuthorizationIntegrationTest`: executado |
| Qualquer rota ainda nao publicada, por exemplo `GET /api/inexistente` | 401 sem token, malformado ou expirado | 404 com JWT valido (a autenticacao passou; a rota nao existe) | 404 com JWT valido (a autenticacao passou; a rota nao existe) | 404 com JWT valido (a autenticacao passou; a rota nao existe) | `SecurityAuthorizationIntegrationTest` |

`POST /api/salas`, `POST /api/professores` e `POST /api/materiais` sao operacoes reais com regra de papel: somente ADMIN possui `ROLE_ADMIN`. A suite final `clean verify` executou 93 testes com 0 falhas, 0 erros e 0 ignorados, incluindo PostgreSQL/Testcontainers 1.21.4 e Flyway V1-V5. As regras futuras de ownership de reservas nao pertencem a esta etapa.

## Matriz planejada

Todas as operacoes abaixo pertencem a requisitos futuros e estao **PENDENTE** de implementacao, autorizacao e teste de integracao.

| Operacao futura | Requisito | Regra esperada | Status |
|---|---|---|---|
| Disponibilidade temporal de recursos | RF-03, RF-04, RF-05 e RF-08 | Deve considerar intervalo solicitado, reservas, conflitos e manutencao; `ativo` nao equivale a disponivel | PENDENTE |
| Criar, alterar e cancelar reservas | RF-04 | SOLICITANTE so pode alterar/cancelar reserva propria; tentativa sobre reserva de outro solicitante deve retornar 403 | PENDENTE |
| Fluxo de reservas, aprovacao e disponibilidade | RF-05 a RF-07 | Permissoes por papel conforme regra de negocio | PENDENTE |
| Manutencao, movimentacao, historico, notificacoes e relatorios | RF-08 a RF-14 | Permissoes por papel conforme regra de negocio | PENDENTE |
