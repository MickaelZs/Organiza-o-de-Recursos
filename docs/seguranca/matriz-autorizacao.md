# Matriz de autorizacao

## Matriz implementada

Os endpoints abaixo existem nesta etapa. A configuracao real do Spring Security e exercitada por testes de integracao com `MockMvc`.

| Operacao | Anonimo | SOLICITANTE | RESPONSAVEL | ADMIN | Evidencia |
|---|---|---|---|---|---|
| `GET /api/health` | 200 | 200 | 200 | 200 | `permiteOperacoesPublicasSemToken` (health sem token) |
| `POST /api/auth/login` | Endpoint acessivel; 2xx com credencial valida / 400 com payload invalido | Mesmo comportamento | Mesmo comportamento | Mesmo comportamento | `permiteOperacoesPublicasSemToken` e `AuthControllerTest` |
| `GET /api/salas` | 401 | 200 | 200 | 200 | `SalaAuthorizationIntegrationTest`; teste criado, execucao pendente |
| `GET /api/salas/{id}` | 401 | 200/404 | 200/404 | 200/404 | `SalaAuthorizationIntegrationTest`; teste criado, execucao pendente |
| `POST /api/salas` | 401 | 403 | 403 | 201 com payload valido; 400 com payload invalido | `SalaAuthorizationIntegrationTest`; teste criado, execucao pendente |
| `GET /api/professores` | 401 | 200 | 200 | 200 | `ProfessorAuthorizationIntegrationTest`; teste criado, execucao pendente |
| `GET /api/professores/{id}` | 401 | 200/404 | 200/404 | 200/404 | `ProfessorAuthorizationIntegrationTest`; teste criado, execucao pendente |
| `POST /api/professores` | 401 | 403 | 403 | 201 com payload valido; 400 com payload invalido; 409 para e-mail duplicado | `ProfessorAuthorizationIntegrationTest`; teste criado, execucao pendente |
| `GET /api/materiais` | 401 | 200 | 200 | 200 | `MaterialAuthorizationIntegrationTest`; teste criado, execucao pendente |
| `GET /api/materiais/{id}` | 401 | 200/404 | 200/404 | 200/404 | `MaterialAuthorizationIntegrationTest`; teste criado, execucao pendente |
| `POST /api/materiais` | 401 | 403 | 403 | 201 com payload valido; 400 para quantidade/payload invalido | `MaterialAuthorizationIntegrationTest`; teste criado, execucao pendente |
| Qualquer rota ainda nao publicada, por exemplo `GET /api/inexistente` | 401 sem token, malformado ou expirado | 404 com JWT valido (a autenticacao passou; a rota nao existe) | 404 com JWT valido (a autenticacao passou; a rota nao existe) | 404 com JWT valido (a autenticacao passou; a rota nao existe) | `SecurityAuthorizationIntegrationTest` |

`POST /api/salas`, `POST /api/professores` e `POST /api/materiais` sao operacoes reais com regra de papel: somente ADMIN possui `ROLE_ADMIN`. A evidencia automatizada foi criada, mas ainda nao deve ser considerada executada enquanto a pendencia de `testCompile` persistir.

## Matriz planejada

Todas as operacoes abaixo pertencem a requisitos futuros e estao **PENDENTE** de implementacao, autorizacao e teste de integracao.

| Operacao futura | Requisito | Regra esperada | Status |
|---|---|---|---|
| Filtros avancados de sala/professor/material | RF-03 | Restricoes por perfil e filtros a definir com os endpoints reais | PENDENTE |
| Criar, alterar e cancelar reservas | RF-04 | SOLICITANTE so pode alterar/cancelar reserva propria; tentativa sobre reserva de outro solicitante deve retornar 403 | PENDENTE |
| Fluxo de reservas, aprovacao e disponibilidade | RF-05 a RF-07 | Permissoes por papel conforme regra de negocio | PENDENTE |
| Manutencao, movimentacao, historico, notificacoes e relatorios | RF-08 a RF-14 | Permissoes por papel conforme regra de negocio | PENDENTE |
