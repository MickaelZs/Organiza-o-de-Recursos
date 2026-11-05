# Matriz de rastreabilidade de requisitos

| Requisito | Implementação principal | Teste/evidência | Status |
|---|---|---|---|
| RF-01 | `Usuario`, `AuthService`, JWT, `SecurityConfig` | `AuthControllerTest`, `AuthServiceTest`, `JwtServiceTest`, `JwtAuthenticationFilterTest`, testes de autorização | IMPLEMENTADO |
| RF-02 | Controllers/services/repositories de `Sala`, `Professor` e `Material` | `SalaAuthorizationIntegrationTest`, `ProfessorAuthorizationIntegrationTest`, `MaterialAuthorizationIntegrationTest`, testes PostgreSQL de repository | IMPLEMENTADO |
| RF-03 | Consultas de busca e disponibilidade temporal | `ResourceSearchApiPostgresTest` | IMPLEMENTADO |
| RF-04 | `ReservaController` e `ReservaService` | `ReservaApiPostgresTest`, `ReservaRepositoryPostgresTest` | IMPLEMENTADO |
| RF-05 | Consultas de conflito e estados bloqueantes | `ReservaApiPostgresTest`, `ResourceSearchApiPostgresTest` | IMPLEMENTADO |
| RF-06 | Locks pessimistas por recurso | `ReservaConcorrenciaPostgresTest`, `ManutencaoReservaConcorrenciaPostgresTest` | IMPLEMENTADO |
| RF-07 | Aprovação/rejeição por RESPONSAVEL | `ReservaApiPostgresTest` | IMPLEMENTADO |
| RF-08 | `Manutencao`, `ManutencaoService` e locks Reserva × Manutenção | `ManutencaoApiRedTest`, `ManutencaoReservaConcorrenciaPostgresTest`, `ManutencaoRepositoryPostgresTest` | IMPLEMENTADO |
| RF-09 | Estados `EM_USO`/`CONCLUIDA`, timestamps e endpoints de retirada/devolução | `ReservaRetiradaDevolucaoPostgresTest` | IMPLEMENTADO |
| RF-10 | `Auditoria`, repository/service/controller e V9 | `AuditoriaPostgresTest`; operações auditadas listadas no relatório final | IMPLEMENTADO |
| RF-11 | `Notificacao`, repository/service/controller e V10 | `NotificacaoPostgresTest`; eventos notificados listados no relatório final | IMPLEMENTADO |
| RF-12 | `RelatorioController`, `RelatorioService`, `RelatorioRepository` e DTOs | `RelatorioPostgresTest`, PostgreSQL/Testcontainers e Flyway V1–V10 | IMPLEMENTADO |
| RF-13 | `ApiError`, handlers globais e respostas JSON de SecurityConfig | `InterfaceErrosIntegrationTest` e testes de autorização/regra | IMPLEMENTADO |
| RF-14 | README, PRD, RTM, status, matriz de autorização e guias de API | Revisão de documentação contra código/testes; build final | IMPLEMENTADO |

## Evidência quantitativa

Execução final em 05/11/2026: `clean verify` com Java 21 terminou em `BUILD SUCCESS`, com 187 testes, 0 falhas, 0 erros e 0 ignorados. JaCoCo global: 870/933 linhas (93,25%) e 259/340 branches (76,18%). Flyway validou e aplicou V1–V10 em PostgreSQL 16.15 limpo, schema final v10.

## Limites

RF13 não inclui frontend visual: o projeto é uma API REST e a documentação disponível não exige uma interface gráfica.
