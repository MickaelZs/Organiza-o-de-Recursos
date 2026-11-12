# Relatório de testes

## Execução local desta auditoria

Data: 12/11/2026. Java 21 confirmado. Docker Desktop 4.97.0/Engine 29.9.2 acessível ao processo Maven; os testes de integração executaram com PostgreSQL 16.15 real via Testcontainers.

Escopo Git: branch `feature/qa-finalizacao-etapas-1-2`, HEAD `f58a68e` (`feat: finalize admin bootstrap and quality gates`, 09/11/2026) e árvore de trabalho com alterações não commitadas. Referências a `d0100a1`, `7948ce7` e `6a758d2` abaixo são históricas e estão identificadas por finalidade; não são o commit da execução atual.

| Medida | Resultado |
|---|---:|
| Testes executados | 214 |
| Aprovados | 214 |
| Falhas | 0 |
| Erros | 0 |
| Ignorados | 0 |
| JaCoCo LINE | 89,22% (1.092/1.224) |
| JaCoCo BRANCH | 72,07% (320/444) |
| `jacoco-check` (limites 80/70) | PASS |

O comando solicitado `.\mvnw.cmd -B verify` terminou com `BUILD SUCCESS`. O Flyway validou e aplicou as 10 migrations em cada banco de teste; o relatório JaCoCo foi gerado e o `jacoco-check` informou `All coverage checks have been met.`

## Evidência histórica preservada

Em 10/11/2026 houve uma execução com os mesmos 214 testes e cobertura, mas o `spring-boot:repackage` falhou ao renomear o JAR no Windows. Essa ocorrência permanece registrada no histórico; não é o resultado final desta retomada.

### Marcos históricos de execução e versão

Os registros abaixo são preservados para não misturar resultados de etapas diferentes. As datas indicadas são as datas dos commits/registros versionados; o relatório original não informa uma data de execução separada.

| Etapa/registro | Versão Git do registro | Data do registro | Comando/ambiente documentado | Testes | JaCoCo LINE | JaCoCo BRANCH | Situação |
|---|---|---|---|---:|---:|---:|---|
| Resultado anterior | `d0100a1` — `main`, merge da entrega | 08/11/2026 | `clean verify`, Java 21 e Docker Desktop | 192, 0 falhas, 0 erros, 0 ignorados | 93,03% (4.616/4.962) | 76,44% (266/348) | Histórico preservado; não representa a árvore atual |
| Etapa 2 | `f58a68e` — `feat: finalize admin bootstrap and quality gates` | 09/11/2026 | `verify`, Java 21 e Docker Desktop | 204, 0 falhas, 0 erros, 0 ignorados | 87,78% (948/1.080) | 70,09% (300/428) | Histórico preservado; substituído pelo resultado atual da auditoria |

## Tipos de teste e limites

- JUnit 5/Jupiter, testes unitários, parametrizados, de integração e API em caixa-preta.
- Testcontainers com `postgres:16-alpine` para persistência, migrations, autorização, auditoria, notificações, relatórios e concorrência.
- Testes concorrentes usam `CountDownLatch`/`ExecutorService`; não substituem o teste JMeter de carga.
- Fluxos de API end-to-end semânticos atravessam Spring MVC, serviços, JPA, Flyway e PostgreSQL. Não foi identificada automação de navegador E2E.
- WireMock não é aplicável ao comportamento atual: não há chamada HTTP externa a provedor; as notificações são simuladas e persistidas internamente.
- TDD RED/GREEN de manutenção está referenciado por `7948ce7` (testes RED, 04/11/2026) e `6a758d2` (implementação, 04/11/2026); não se infere TDD para toda a suíte.
- Nenhum teste foi desabilitado para obter o resultado acima; não foram usados `-DskipTests`, exclusões de classes ou redução de gates.

## Tentativa diagnóstica sem acesso ao Docker

Data: 12/11/2026. A primeira tentativa sem elevação do processo Maven recebeu `AccessDeniedException` ao acessar `\\.\pipe\docker_engine`.

| Medida | Resultado |
|---|---:|
| Testes executados | 93 |
| Falhas | 0 |
| Erros | 14 |
| Ignorados | 9 |
| Causa | sandbox sem acesso ao named pipe do Docker |

Essa tentativa foi mantida como diagnóstico do ambiente. A execução escalada
posterior, registrada na seção principal, conectou-se ao Docker e concluiu a
suíte sem falhas, erros ou testes ignorados.

**Regra de consolidação:** 93 é a contagem de uma tentativa parcial e não deve
ser somada nem comparada como resultado funcional final. O resultado oficial
desta auditoria é a suíte completa de 214 testes, com 0 falhas, 0 erros e 0
ignorados.

## Estado remoto

O workflow está definido em `.github/workflows/ci.yml`. A execução remota consultada mais recente foi bem-sucedida em `main`, no commit `d0100a1` (08/11/2026), anterior ao HEAD `f58a68e` (09/11/2026) e às alterações locais desta auditoria. Não há evidência de CI/SonarCloud para a árvore de trabalho atual. O scanner SonarCloud é condicional às variáveis/secrets do repositório; a configuração do YAML não prova que o scanner ou o Quality Gate rodaram.

## Comando de rastreabilidade

Após `verify`, `java scripts/ValidateRtm.java` foi executado com JDK 21 e retornou `RTM structural validation passed`. O verificador não altera os resultados funcionais nem substitui revisão dos testes; ele valida links, IDs, referências, arquivos de evidência e resultados Surefire para linhas marcadas `PASS`.
