# Relatório final de qualidade

## Escopo

Auditoria do sistema acadêmico contra os requisitos funcionais, regras críticas e evidências de qualidade solicitadas.

## Resultado resumido

| Área | Resultado |
|---|---|
| Requisitos funcionais RF-01 a RF-14 | PASS por código e testes; rastreabilidade em `docs/rtm.md` |
| Segurança | PASS nos testes versionados; segredo de produção depende de configuração externa |
| PostgreSQL/Testcontainers | EXECUTADO E COMPROVADO nos testes `*PostgresTest` |
| Concorrência | EXECUTADO E COMPROVADO com `CountDownLatch`/`ExecutorService` |
| TDD | EXECUTADO E COMPROVADO no histórico RF08 |
| JaCoCo | EXECUTADO E COMPROVADO; 87,78% de linhas e 70,09% de branches, `jacoco:check` aprovado com metas 80/70 |
| Boot local | PASS; PostgreSQL 18.4, Flyway V1–V10, Tomcat 8080 e aplicação iniciada |
| Autenticação local | PASS; login normal HTTP 200 e endpoint protegido HTTP 200 |
| JMeter | PASS; 100 samples, 100 HTTP 200, 0 erros e error rate 0% |
| GitHub Actions | CONFIGURADO; execução remota disponível para o commit anterior, mas a Etapa 2 local ainda não foi publicada |
| SonarCloud | CONFIGURADO condicionalmente; há Quality Gate aprovado no commit remoto anterior, não atribuível às alterações locais da Etapa 2 |
| WireMock | NÃO APLICÁVEL: não há integração HTTP externa |

## Evidência de performance

Em 08/11/2026, a execução local do JMeter 5.6.3 usou Java 21, PostgreSQL 18.4, 10 usuários virtuais, 10 iterações por usuário e ramp-up de 30 s contra `GET /api/salas?tipo=Aula`. Foram observadas 100 amostras, todas HTTP 200, error rate 0%, average 7,17 ms, median 6,00 ms, p90 8,00 ms, p95 9,95 ms, p99 61,60 ms, mínimo 5,00 ms, máximo 62,00 ms e throughput 3,7288 samples/s. O detalhamento está em [resultado-jmeter.md](../performance/resultado-jmeter.md).

## Riscos e pendências

1. O usuário de performance criado para esta execução é dado local de teste e não faz parte do código ou versionamento.
2. Resultado SonarCloud da Etapa 2 depende de `SONAR_TOKEN`, organização e projeto configurados no GitHub.
3. A execução real do workflow para esta árvore precisa ser observada no GitHub após publicação; a presença do YAML e a execução do commit anterior não são prova da Etapa 2.
4. A inspeção visual manual da interface permanece pendente, se exigida pela avaliação.

## Conclusão

O núcleo funcional, os testes acadêmicos, o boot local, a autenticação e a execução real do JMeter estão implementados e evidenciados. Permanecem como pendências externas a execução/quality gate do SonarCloud, a comprovação do workflow no GitHub e a inspeção visual manual, caso obrigatórias.
