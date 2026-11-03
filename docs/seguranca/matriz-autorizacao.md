# Matriz de autorizacao

Revisada na auditoria final de 03/11/2026. `clean verify`: 147 testes, 0 falhas, 0 erros e 0 ignorados (BUILD SUCCESS). Os testes de JWT/autorização e reservas foram executados; os cenários com persistência usam PostgreSQL/Testcontainers. Detalhes de cobertura e migrations: `docs/rtm.md`.

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
| `GET /api/salas` com filtros `tipo`, `capacidadeMinima`, `localizacao` | 401 | 200 | 200 | 200 | `ResourceSearchApiPostgresTest`: 24 testes executados na suite final, 0 falhas/erros |
| `GET /api/professores` com filtro `competencia` | 401 | 200 | 200 | 200 | `ResourceSearchApiPostgresTest`: 24 testes executados na suite final, 0 falhas/erros |
| `GET /api/materiais` com filtros `tipo`, `localizacao` | 401 | 200 | 200 | 200 | `ResourceSearchApiPostgresTest`: 24 testes executados na suite final, 0 falhas/erros |
| `GET /api/materiais/{id}` | 401 | 200/404 | 200/404 | 200/404 | `MaterialAuthorizationIntegrationTest`: executado |
| `POST /api/materiais` | 401 | 403 | 403 | 201 com payload valido; 400 para quantidade/payload invalido | `MaterialAuthorizationIntegrationTest`: executado |
| `POST /api/reservas` | 401 | 201/400/404 | 403 | 403 | `ReservaApiPostgresTest` (33 testes): criação, validação, referências, conflitos e ownership; somente SOLICITANTE solicita |
| `POST /api/reservas/{id}/aprovar` | 401 | 403 | 200/403/404/409 | 403 | `ReservaApiPostgresTest` (33 testes): somente RESPONSAVEL aprova solicitação pendente de recurso restrito; conflito revalidado |
| `POST /api/reservas/{id}/rejeitar` | 401 | 403 | 200/403/404/409 | 403 | `ReservaApiPostgresTest` (33 testes): somente RESPONSAVEL rejeita solicitação pendente de recurso restrito |
| `GET /api/reservas` | 401 | 200, somente próprias | 200, todas | 200, todas | `ReservaApiPostgresTest`: filtro por solicitante e consulta de equipe |
| `GET /api/reservas/{id}` | 401 | 200 própria / 403 alheia / 404 inexistente | 200/404 | 200/404 | `ReservaApiPostgresTest`: ownership na consulta |
| `PUT /api/reservas/{id}` | 401 | 200 própria / 403 alheia / 400 inválida / 404 inexistente | 403 | 403 | `ReservaApiPostgresTest`: alteração restrita ao solicitante proprietário |
| `DELETE /api/reservas/{id}` | 401 | 204 própria / 403 alheia / 404 inexistente | 403 | 403 | `ReservaApiPostgresTest`: cancelamento lógico; registro preservado |
| Qualquer rota ainda nao publicada, por exemplo `GET /api/inexistente` | 401 sem token, malformado ou expirado | 404 com JWT valido (a autenticacao passou; a rota nao existe) | 404 com JWT valido (a autenticacao passou; a rota nao existe) | 404 com JWT valido (a autenticacao passou; a rota nao existe) | `SecurityAuthorizationIntegrationTest` |

`POST /api/salas`, `POST /api/professores` e `POST /api/materiais` sao operacoes reais com regra de papel: somente ADMIN possui `ROLE_ADMIN`. Em reservas, somente SOLICITANTE pode criar, alterar e cancelar; solicitantes consultam apenas as próprias reservas e RESPONSAVEL/ADMIN consultam todas. Somente RESPONSAVEL aprova ou rejeita solicitações pendentes de recursos restritos. Alteração e cancelamento de solicitante ficam limitados à reserva cujo proprietário corresponde ao subject do JWT.

## Operações futuras

As operações abaixo ainda pertencem a requisitos futuros e estão **PENDENTE** de implementação, autorização e teste de integração.

| Operação futura | Requisito | Regra esperada | Status |
|---|---|---|---|
| Bloqueio e consulta de períodos de manutenção | RF-08 | Recurso em manutenção não pode ser reservado nem apresentado como disponível | PENDENTE |
| Retirada/devolução, histórico, notificações, relatórios e interface | RF-09 a RF-14 | Permissões e fluxos conforme cada regra de negócio | PENDENTE |
