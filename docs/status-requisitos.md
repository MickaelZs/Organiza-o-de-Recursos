# Status dos requisitos

| Requisito | Implementado | Validado por | Status |
|---|---|---|---|
| RF-01 | Sim | Autenticação JWT, perfis e testes de autorização | PASS |
| RF-02 | Sim | APIs e repositories PostgreSQL | PASS |
| RF-03 | Sim | Busca e disponibilidade em `ResourceSearchApiPostgresTest` | PASS |
| RF-04 | Sim | Reservas em `ReservaApiPostgresTest` e repository | PASS |
| RF-05 | Sim | Conflitos e estados bloqueantes | PASS |
| RF-06 | Sim | `ReservaConcorrenciaPostgresTest` | PASS |
| RF-07 | Sim | Aprovação/rejeição de restritos | PASS |
| RF-08 | Sim | Manutenção e concorrência Reserva × Manutenção | PASS |
| RF-09 | Sim | `ReservaRetiradaDevolucaoPostgresTest` | PASS |
| RF-10 | Sim | `AuditoriaPostgresTest` e V9 | PASS |
| RF-11 | Sim | `NotificacaoPostgresTest` e V10 | PASS |
| RF-12 | Sim | `RelatorioPostgresTest` e PostgreSQL/Testcontainers | PASS |
| RF-13 | Sim | `InterfaceErrosIntegrationTest` e contrato REST | PASS |
| RF-14 | Sim | Documentação revisada contra implementação e evidências | PASS |

## Evidência final

`clean verify` final (Java 21, 05/11/2026): BUILD SUCCESS; 187 testes; 0 falhas; 0 erros; 0 ignorados. JaCoCo global: 870/933 linhas (93,25%) e 259/340 branches (76,18%). Flyway aplicou V1–V10 em PostgreSQL 16.15 limpo e alcançou o schema v10. A matriz detalhada está em `docs/rtm.md`.
