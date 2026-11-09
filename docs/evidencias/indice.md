# Índice de evidências

| Tema | Evidência |
|---|---|
| Requisitos e riscos | [RTM](../rtm.md), [status](../status-requisitos.md), [PRD](../prd.md) |
| API e erros | [API de recursos](../api-recursos.md), [API de relatórios](../api-relatorios.md), [contrato de erros](../api-erros.md), `InterfaceErrosIntegrationTest` |
| Segurança | [matriz de autorização](../seguranca/matriz-autorizacao.md), `SecurityConfig`, `JwtService`, `JwtAuthenticationFilter`, testes de segurança |
| Schema | migrations `V1__foundation.sql` até `V10__create_notificacoes.sql` |
| Reservas | `ReservaService`, `ReservaApiPostgresTest`, `ReservaRepositoryPostgresTest` |
| Concorrência | `ReservaConcorrenciaPostgresTest`, `ManutencaoReservaConcorrenciaPostgresTest`, ADR-0003 |
| Manutenção/TDD | `ManutencaoApiRedTest`, commits `7948ce7` RED e `6a758d2` GREEN |
| Retirada/devolução | `ReservaRetiradaDevolucaoPostgresTest`, migration V8 |
| Auditoria | `AuditoriaPostgresTest`, `AuditoriaService`, migration V9 |
| Notificação | `NotificacaoPostgresTest`, `NotificacaoService`, migration V10 |
| Relatórios | `RelatorioPostgresTest`, `RelatorioService`, [API de relatórios](../api-relatorios.md) |
| Interface | `src/main/resources/static/index.html`, `styles.css`, `app.js`, `InterfaceErrosIntegrationTest` |
| CI/coverage | `.github/workflows/ci.yml`, `pom.xml`, `target/site/jacoco/` após `clean verify` |
| Performance | [plano JMeter](../performance/plano-jmeter.md), [resultado real](../performance/resultado-jmeter.md); 100 samples, HTTP 200 em todas e error rate 0% |
| Defeitos | [registro de defeitos](../defeitos/registro-defeitos.md) |
