# Matriz de autorização

A matriz abaixo corresponde às regras reais de `SecurityConfig` e às verificações de ownership dos services.

Evidência final: `clean verify` com Java 21 e Docker Desktop, 192 testes, 0 falhas, 0 erros, 0 ignorados, BUILD SUCCESS. O relatório está em `docs/testes/relatorio-testes.md`.

| Operação | Anônimo | SOLICITANTE | RESPONSAVEL | ADMIN |
|---|---:|---:|---:|---:|
| `GET /api/health` | 200 | 200 | 200 | 200 |
| `POST /api/auth/login` | permitido | permitido | permitido | permitido |
| Consultas de salas/professores/materiais | 401 | permitido | permitido | permitido |
| Criação de sala/professor/material | 401 | 403 | 403 | permitido |
| `POST /api/reservas` | 401 | permitido | 403 | 403 |
| Consulta de reservas | 401 | próprias | todas | todas |
| Alteração/cancelamento de reserva | 401 | própria | 403 | 403 |
| Aprovação/rejeição de reserva restrita | 401 | 403 | permitido | 403 |
| Criação/consulta/cancelamento de manutenção | 401 | 403 | permitido | permitido |
| Retirada/devolução de reserva | 401 | 403 | permitido | permitido |
| `GET /api/auditorias` ou `/api/auditoria` | 401 | 403 | permitido | permitido |
| `GET /api/notificacoes` | 401 | próprias | próprias | próprias |
| `PATCH /api/notificacoes/{id}/lida` | 401 | própria | própria | própria |
| `GET /api/relatorios/**` | 401 | 403 | permitido | permitido |

## Regras de ownership

O solicitante só altera, cancela e consulta a própria reserva. Notificações são filtradas pelo usuário autenticado; a marcação como lida rejeita notificações de outro usuário. Auditoria e relatórios são administrativos/operacionais e não são expostos a SOLICITANTE.

## Contrato de segurança

401 e 403 são respostas JSON padronizadas por `SecurityConfig`, sem stack trace. São públicas a interface estática, documentação Springdoc/OpenAPI, health e login; as rotas de negócio continuam exigindo autenticação e, com JWT válido, uma rota inexistente retorna 404.
