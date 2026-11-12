# Relatório final de qualidade

## Escopo

Auditoria do sistema acadêmico contra os requisitos funcionais, regras críticas e evidências de qualidade solicitadas.

## Resultado resumido

| Área | Resultado |
|---|---|
| Requisitos funcionais RF-01 a RF-14 | RTM fechada localmente; RF-13/RF-14 permanecem PARCIAIS por inspeção visual/revisão de clone |
| Segurança | PASS nos testes versionados; segredo de produção depende de configuração externa |
| PostgreSQL/Testcontainers | EXECUTADO LOCALMENTE e sustentado pelos relatórios Surefire de 12/11/2026; os relatórios brutos não são versionados |
| Concorrência | EXECUTADA LOCALMENTE e sustentada pelos testes com `CountDownLatch`/`ExecutorService`; não representa capacidade de produção |
| TDD | HISTÓRICO LOCAL COMPROVADO por `7948ce7` (testes RED de manutenção) seguido de `6a758d2` (implementação); não representa o HEAD atual |
| JaCoCo | EXECUTADO LOCALMENTE: 89,22% de linhas e 72,07% de branches; `jacoco:check` aprovado com metas 80/70 |
| Boot local | OBSERVAÇÃO HISTÓRICA LOCAL; não há log de inicialização versionado para comprovar independentemente PostgreSQL 18.4, Flyway V1–V10, Tomcat 8080 e a sessão manual |
| Autenticação local | TESTES AUTOMATIZADOS LOCAIS aprovados; não há gravação ou log versionado de uma sessão manual de login e acesso protegido |
| JMeter | PASS LOCAL somente para o cenário documentado; 100 samples, 100 HTTP 200 e 0 erros. JTL/HTML permanecem em `target/` ignorado |
| GitHub Actions | CONFIGURADO; execução remota anterior não representa a árvore atual não publicada |
| SonarCloud | CONFIGURADO condicionalmente; Quality Gate desta árvore não comprovado |
| WireMock | NÃO APLICÁVEL: não há integração HTTP externa |

## Evidência de performance

Em 08/11/2026, a execução local do JMeter 5.6.3 usou Java 21, PostgreSQL 18.4, 10 usuários virtuais, 10 iterações por usuário e ramp-up de 30 s contra `GET /api/salas?tipo=Aula`. Foram observadas 100 amostras, todas HTTP 200, error rate 0%, average 7,17 ms, median 6,00 ms, p90 8,00 ms, p95 9,95 ms, p99 61,60 ms, mínimo 5,00 ms, máximo 62,00 ms e throughput 3,7288 samples/s. O detalhamento está em [resultado-jmeter.md](../performance/resultado-jmeter.md).

Esses números são uma observação local baseada nos artefatos JTL/HTML existentes em `target/`; como esses arquivos são ignorados pelo Git, o relatório agregado não constitui prova portável nem substitui uma nova execução.

## Histórico de resultados preservado

Para manter a distinção entre versões, os registros anteriores permanecem documentados em [relatório de testes](../testes/relatorio-testes.md): `d0100a1` (`main`, 08/11/2026) registrou 192 testes, 93,03% LINE e 76,44% BRANCH; `f58a68e` (09/11/2026) registrou 204 testes, 87,78% LINE e 70,09% BRANCH. O resultado vigente desta auditoria é o da árvore de trabalho de 12/11/2026, com 214 testes, 89,22% LINE e 72,07% BRANCH.

## Riscos e pendências

1. O usuário de performance criado para esta execução é dado local de teste e não faz parte do código ou versionamento.
2. Resultado SonarCloud das Etapas 2 e 3 depende de `SONAR_TOKEN`, organização e projeto configurados no GitHub.
3. A execução real do workflow para esta árvore precisa ser observada no GitHub após publicação; a presença do YAML e a execução do commit anterior não são prova das Etapas 2 e 3.
4. A inspeção visual manual da interface permanece pendente, se exigida pela avaliação.

## Conclusão

O núcleo funcional, os testes acadêmicos, a autenticação, a validação PostgreSQL e o `verify` completo estão evidenciados pela execução de 12/11/2026. Permanecem pendentes a execução remota CI/SonarCloud para esta árvore, inspeção visual manual, revisão em clone limpo e atribuição versionada das contribuições dos quatro integrantes.
