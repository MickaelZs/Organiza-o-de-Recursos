# Relatório de testes

## Execução do commit avaliado

- Data: 12/11/2026.
- Branch: feature/qa-finalizacao-etapas-1-2.
- Commit: 3426e518d9993c5db54de088d30773fb60ac1f40.
- Clone: shallow, novo e temporário, obtido do GitHub; HEAD conferido antes do build.
- Comando: .\mvnw.cmd -B clean verify.
- Ambiente: Java 21.0.11, Docker Engine 29.9.2, PostgreSQL 16.15 por Testcontainers.
- Resultado local em clone limpo: BUILD SUCCESS; 214 testes, 0 falhas, 0 erros e 0 ignorados.
- Resultado remoto: GitHub Actions run 43265545752, build SUCCESS; artifact test-reports disponível.

| Medida | Clone limpo local | Artifact CI |
|---|---:|---:|
| Testes executados | 214 | 214 |
| Falhas | 0 | 0 |
| Erros | 0 | 0 |
| Ignorados | 0 | 0 |
| JaCoCo LINE | 90,01% (1.072/1.191) | 89,84% (1.070/1.191) |
| JaCoCo BRANCH | 72,07% (320/444) | 72,07% (320/444) |
| Gates JaCoCo 80/70 | PASS | PASS |

Os percentuais locais e remotos diferem em duas linhas cobertas; ambos os
relatórios vêm de clean verify e passam o mesmo limite. A causa dessa variação
não foi determinada; os números são mantidos separados por origem.

O clone executou também java scripts\ValidateRtm.java: 24 IDs e 174 links
locais verificados, com RTM structural validation passed. O Flyway aplicou as
10 migrations nos bancos descartáveis iniciados pelos testes.

## Tipos e limites da suíte

- JUnit 5/Jupiter, unitários, parametrizados, integração e API via MockMvc.
- Testcontainers com PostgreSQL 16; não há substituição por H2.
- Testes de autorização, transições, auditoria, relatórios, notificações,
  bootstrap e concorrência executaram na suíte.
- Os testes concorrentes usam CountDownLatch/ExecutorService e verificam o
  estado persistido no PostgreSQL; são distintos de medições de carga.
- A aplicação não possui automação de navegador E2E. MockMvc cobre fluxos de
  API através do Spring e da persistência, mas não substitui inspeção visual.
- WireMock não se aplica ao código atual porque não há cliente HTTP externo;
  notificações são persistidas internamente.
- A sequência RED/GREEN de manutenção está documentada por 7948ce7 (teste) e
  6a758d2 (implementação), como evidência histórica.
- Nenhum teste foi ignorado ou excluído; não se usou -DskipTests.

## Evidência de integração contínua

- [Execução do GitHub Actions](https://github.com/MickaelZs/Organiza-o-de-Recursos/actions/runs/43265545752)
- [PR #9](https://github.com/MickaelZs/Organiza-o-de-Recursos/pull/9)
- O artifact test-reports contém Surefire e JaCoCo do commit
  3426e518d9993c5db54de088d30773fb60ac1f40.
- SonarCloud e Quality Gate estão detalhados em
  [docs/entrega/sonarcloud.md](../entrega/sonarcloud.md).

## Histórico separado

| Commit | Testes | LINE | BRANCH | Uso |
|---|---:|---:|---:|---|
| d0100a1 (main, 08/11/2026) | 192 | 93,03% | 76,44% | Histórico |
| f58a68e (09/11/2026) | 204 | 87,78% | 70,09% | Histórico |
| 3426e51 (12/11/2026) | 214 | 90,01% local / 89,84% CI | 72,07% | Commit avaliado |

Histórico de tentativa diagnóstica com Docker indisponível não é somado nem
usado como resultado funcional.

## Evidências não automatizadas

O clone limpo validou o build e os testes; não executou o servidor, o bootstrap
interativo do primeiro administrador ou login pela interface. O relatório
JMeter desta missão está em [resultado-01.md](../performance/resultado-01.md):
os JTL/HTML de execuções antigas não estão disponíveis e não foi feita uma nova
carga nesta auditoria.
