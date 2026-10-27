# Matriz de autorizacao

## Matriz implementada

Os unicos endpoints funcionais existentes nesta etapa sao os abaixo. A configuracao real do Spring Security e exercitada por `SecurityAuthorizationIntegrationTest`.

| Operacao | Anonimo | SOLICITANTE | RESPONSAVEL | ADMIN | Evidencia |
|---|---|---|---|---|---|
| `GET /api/health` | 200 | 200 | 200 | 200 | `permiteOperacoesPublicasSemToken` (health sem token) |
| `POST /api/auth/login` | Endpoint acessivel; 2xx com credencial valida / 400 com payload invalido | Mesmo comportamento | Mesmo comportamento | Mesmo comportamento | `permiteOperacoesPublicasSemToken` e `AuthControllerTest` |
| Qualquer rota ainda nao publicada, por exemplo `GET /api/inexistente` | 401 sem token, malformado ou expirado | 404 com JWT valido (a autenticacao passou; a rota nao existe) | 404 com JWT valido (a autenticacao passou; a rota nao existe) | 404 com JWT valido (a autenticacao passou; a rota nao existe) | `SecurityAuthorizationIntegrationTest` |

Nao ha endpoint real com regra de papel nesta etapa. Portanto, nenhum 403 por papel e alegado como coberto. O `403` esta configurado para uma futura operacao protegida que negue um usuario autenticado sem a authority exigida, mas essa regra ainda nao pode ser exercitada sem criar um endpoint artificial.

## Matriz planejada

Todas as operacoes abaixo pertencem a requisitos futuros e estao **PENDENTE** de implementacao, autorizacao e teste de integracao.

| Operacao futura | Requisito | Regra esperada | Status |
|---|---|---|---|
| Gestao de salas, professores e materiais | RF-02 e RF-03 | Restricoes por perfil a definir com os endpoints reais | PENDENTE |
| Criar, alterar e cancelar reservas | RF-04 | SOLICITANTE so pode alterar/cancelar reserva propria; tentativa sobre reserva de outro solicitante deve retornar 403 | PENDENTE |
| Fluxo de reservas, aprovacao e disponibilidade | RF-05 a RF-07 | Permissoes por papel conforme regra de negocio | PENDENTE |
| Manutencao, movimentacao, historico, notificacoes e relatorios | RF-08 a RF-14 | Permissoes por papel conforme regra de negocio | PENDENTE |
