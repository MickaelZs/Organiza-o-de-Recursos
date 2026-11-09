# Matriz de rastreabilidade de requisitos

| Requisito | Risco | Implementação | Teste | Evidência | Status | Pendência |
|---|---|---|---|---|---|---|
| RF-01 Autenticação/autorização | acesso indevido | `Usuario`, JWT, `SecurityConfig`, BCrypt | `AuthControllerTest`, `AuthServiceTest`, testes JWT/autorização | matriz de autorização, código e relatórios de teste | PASS | nenhuma funcional; secret de produção externo |
| RF-02 Cadastro/consulta de recursos | dados inválidos/incompletos | controllers/services/repositories de salas, professores e materiais | testes de autorização e repositories PostgreSQL | migrations V3–V5 e testes `*AuthorizationIntegrationTest` | PASS | nenhuma |
| RF-03 Busca e disponibilidade | alocação de recurso ocupado | Specifications de filtros e disponibilidade | `ResourceSearchApiPostgresTest`, manutenção concorrente | `docs/api-recursos.md`, código e testes | PASS | nenhuma |
| RF-04 Criar/alterar/cancelar reserva | alteração indevida ou perda de histórico | `ReservaController` e `ReservaService` | `ReservaApiPostgresTest`, `ReservaRepositoryPostgresTest` | migration V6, API e auditoria | PASS | nenhuma |
| RF-05 Conflitos de sala/professor/material | sobreposição | consulta de intervalos semiabertos e estados bloqueantes | `ReservaApiPostgresTest`, `ResourceSearchApiPostgresTest` | ADR-0002 e testes parametrizados | PASS | nenhuma |
| RF-06 Double booking concorrente | duas confirmações incompatíveis | locks `PESSIMISTIC_WRITE` por recurso | `ReservaConcorrenciaPostgresTest`, `ManutencaoReservaConcorrenciaPostgresTest` | ADR-0003 e execução PostgreSQL | PASS | nenhuma |
| RF-07 Aprovação de restritos | aprovação por perfil errado | `SOLICITADA` e guarda exclusiva de `RESPONSAVEL` | cenários de aprovação/rejeição em `ReservaApiPostgresTest` | ADR-0004, SecurityConfig e testes 403 | PASS | nenhuma |
| RF-08 Manutenção | reservar recurso bloqueado | `ManutencaoService`, status e conflito cruzado | `ManutencaoApiRedTest`, concorrência Reserva × Manutenção | migration V7, RED/GREEN e ADR-0003 | PASS | nenhuma |
| RF-09 Retirada/devolução | uso sem retirada ou devolução duplicada | transições `APROVADA → EM_USO → CONCLUIDA` | `ReservaRetiradaDevolucaoPostgresTest` | migration V8 e auditoria | PASS | nenhuma |
| RF-10 Histórico auditável | alteração sem rastreabilidade | `AuditoriaService`, registros de estado e `ALTERADA` | `AuditoriaPostgresTest` | migration V9 e `docs/defeitos/registro-defeitos.md` | PASS | nenhuma |
| RF-11 Notificação | usuário não informado | notificações persistidas e filtradas por destinatário | `NotificacaoPostgresTest` | migration V10 e API de notificações | PASS | WireMock não aplicável sem HTTP externo |
| RF-12 Relatórios | números inconsistentes | consultas de utilização, carga e conflitos evitados | `RelatorioPostgresTest` | `docs/api-relatorios.md` e PostgreSQL | PASS | nenhuma |
| RF-13 Interface/erros | experiência incompreensível ou vazamento | HTML/CSS/JS responsivos e handlers 400–500 | `InterfaceErrosIntegrationTest` | assets estáticos e contrato de erros | PASS | inspeção visual manual não automatizada |
| RF-14 Documentação pública | entrega não reproduzível | README, OpenAPI, guias, ADRs e diagramas | revisão contra código/testes | este índice, RTM e documentos em `docs/` | PASS | JMeter local comprovado; Sonar depende de execução externa |

## Evidência quantitativa

A execução final comprovada foi `clean verify` com Java 21 e Docker Desktop: 192 testes, 0 falhas, 0 erros, 0 ignorados, `BUILD SUCCESS`, 93,03% de linhas (4.616/4.962) e 76,44% de branches (266/348). O relatório detalhado está em `docs/testes/relatorio-testes.md`.

## Limites

RF13 inclui a interface web estática existente em `/`, além do contrato REST e das respostas de erro. Não há framework frontend separado.
