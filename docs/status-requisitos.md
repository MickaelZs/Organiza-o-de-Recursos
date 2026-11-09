# Status dos requisitos

| Requisito | Implementado | Validado por | Status |
|---|---|---|---|
| RF-01 Autenticação e autorização | Sim | JWT, perfis, BCrypt e testes de autorização | PASS |
| RF-02 Cadastro e consulta | Sim | APIs e repositories PostgreSQL | PASS |
| RF-03 Busca e disponibilidade | Sim | `ResourceSearchApiPostgresTest` e manutenção | PASS |
| RF-04 Reservas | Sim | `ReservaApiPostgresTest` e repository | PASS |
| RF-05 Sobreposição | Sim | conflitos de sala/professor/material | PASS |
| RF-06 Concorrência | Sim | `ReservaConcorrenciaPostgresTest` | PASS |
| RF-07 Aprovação restrita | Sim | aprovação exclusiva de `RESPONSAVEL` | PASS |
| RF-08 Manutenção | Sim | RED/GREEN e concorrência Reserva × Manutenção | PASS |
| RF-09 Retirada/devolução | Sim | `ReservaRetiradaDevolucaoPostgresTest` | PASS |
| RF-10 Auditoria | Sim | `AuditoriaPostgresTest`, V9 e alteração corrigida | PASS |
| RF-11 Notificação | Sim | `NotificacaoPostgresTest` e V10 | PASS |
| RF-12 Relatórios | Sim | `RelatorioPostgresTest` e PostgreSQL | PASS |
| RF-13 Interface/erros | Sim | interface responsiva e `InterfaceErrosIntegrationTest` | PASS |
| RF-14 Documentação | Sim | README, OpenAPI, ADRs, RTM e diagramas | PASS |

## Evidência final

Execução final comprovada: `clean verify` com Java 21 e Docker Desktop, BUILD SUCCESS, 192 testes, 0 falhas, 0 erros, 0 ignorados, 93,03% de linhas (4.616/4.962) e 76,44% de branches (266/348). A matriz detalhada está em `docs/rtm.md` e o relatório está em `docs/testes/relatorio-testes.md`.
