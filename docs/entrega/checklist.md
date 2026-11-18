# Checklist de entrega

Auditoria externa em clone temporário novo do commit `080251a54165ed16e0a4c73a1e6be2b4e22b7b6` da branch `main`.

| Item | Existe | Atualizado | Caminho |
|---|---|---|---|
| Repositório público com revisão por pares | Parcial | Não no commit avaliado: PR #10 e PR #11 têm checks aprovados, mas o GitHub não registra reviews/aprovação por pares | Repositório: https://github.com/MickaelZs/Organiza-o-de-Recursos ; PR #11: https://github.com/MickaelZs/Organiza-o-de-Recursos/pull/11 |
| Aplicação executável | Parcial | O código gera projeto Spring Boot, mas não foi possível iniciar nem produzir o JAR nesta avaliação; o daemon Docker estava indisponível e o `verify` terminou antes do empacotamento | [pom.xml](../../pom.xml), [src/main/java/](../../src/main/java/) |
| README | Sim | Presente no HEAD avaliado; contém pré-requisitos, instalação Windows/Linux, bootstrap, execução e testes | [README.md](../../README.md) |
| Documentação da API | Sim | Presente no HEAD avaliado | [api-fluxos.md](../api-fluxos.md), [api-recursos.md](../api-recursos.md), [api-relatorios.md](../api-relatorios.md), [api-erros.md](../api-erros.md) |
| RTM | Sim | Presente no HEAD avaliado; não foi validada nesta execução porque o `verify` falhou antes da etapa válida da suíte | [rtm.md](../rtm.md) |
| Diagramas de contexto | Sim | Diagramas Mermaid presentes no HEAD avaliado | [diagramas/arquitetura.md](../diagramas/arquitetura.md) |
| Diagramas de componentes | Sim | Diagramas Mermaid presentes no HEAD avaliado | [diagramas/arquitetura.md](../diagramas/arquitetura.md) |
| Diagramas de sequências críticas | Sim | A sequência de reserva concorrente está documentada no HEAD avaliado | [diagramas/arquitetura.md](../diagramas/arquitetura.md) |
| Plano de testes | Sim | Presente no HEAD avaliado | [plano-testes.md](../testes/plano-testes.md) |
| Relatório de testes | Sim | Presente, mas contém resultados históricos de 12/11/2026 e não prova o HEAD nem o resultado desta execução | [relatorio-testes.md](../testes/relatorio-testes.md) |
| Pipeline de CI | Sim | Workflow presente e a execução da `main` no SHA avaliado terminou com sucesso no GitHub | [.github/workflows/ci.yml](../../.github/workflows/ci.yml), https://github.com/MickaelZs/Organiza-o-de-Recursos/actions/runs/43345688930 |
| Relatório SonarCloud | Parcial | Documento presente, mas registra principalmente PR #9/commit 3426e51; PR #11 e o HEAD 080251a têm checks separados aprovados | [sonarcloud.md](sonarcloud.md), https://sonarcloud.io/dashboard?id=MickaelZs_Organiza-o-de-Recursos&pullRequest=11 |
| Relatório JaCoCo | Não nesta execução | Documentos e histórico citam cobertura, mas não existe `target/site/jacoco/index.html` nem `target/site/jacoco/jacoco.xml` no clone limpo após a falha | [relatorio-testes.md](../testes/relatorio-testes.md), saída esperada: `target/site/jacoco/` |
| Plano de teste de carga | Sim | Plano versionado presente no HEAD avaliado | [plano-jmeter.md](../performance/plano-jmeter.md) |
| Resultado do teste de carga | Parcial | Existem resultados documentados, mas não houve JMeter/JTL/HTML atual reproduzido nesta avaliação | [resultado-01.md](../performance/resultado-01.md), [resultado-jmeter.md](../performance/resultado-jmeter.md), [evidências JMeter](../evidencias/jmeter-2026-11-12/) |
| Registro de defeitos | Sim | Presente no HEAD avaliado | [registro-defeitos.md](../defeitos/registro-defeitos.md) |
| Relatório final de qualidade | Parcial | Arquivos presentes, mas o relatório principal descreve auditórias anteriores e sinais pendentes; o relatório de entrega é uma ponte compatível | [relatorio-final-qualidade.md](relatorio-final-qualidade.md), [relatorio-qualidade.md](../relatorio-qualidade.md) |

## Execução observada

1. Foi criado um clone novo em diretório temporário, sem reutilizar `target/` ou dados do workspace.
2. A revisão do README encontrou Java 21, Docker, Maven Wrapper e PostgreSQL como pré-requisitos.
3. Java 21.0.11 estava disponível, mas `JAVA_HOME` não estava definido; a primeira execução de `./mvnw -B verify` falhou com `JAVA_HOME not found in your environment`.
4. `JAVA_HOME` foi configurado a partir do `java.home` ativo e o comando foi repetido.
5. Docker Engine apareceu no PATH, mas o daemon não estava acessível no named pipe do Docker Desktop; o Desktop não foi encontrado no caminho padrão desta máquina.
6. `./mvnw -B verify` terminou em `BUILD FAILURE`: 93 testes executados, 14 erros, 0 falhas e 9 ignorados. Os 14 erros eram testes PostgreSQL/Testcontainers que não encontraram Docker.
7. Não foi possível iniciar PostgreSQL, bootstrap, servidor ou login, pois o README exige Docker ativo para os testes e o ambiente não disponibilizou o daemon.
8. Não houve JAR gerado pelo ciclo desta avaliação nem relatório JaCoCo produzido.

## Bloqueadores em ordem

1. Iniciar o Docker Desktop/daemon e disponibilizar o socket para Testcontainers.
2. Reexecutar `./mvnw -B verify` com Docker ativo até obter build verde no commit final.
3. Gerar e executar o JAR seguindo o README, incluindo PostgreSQL novo, bootstrap do primeiro ADMIN, JWT e login.
4. Gerar novamente JaCoCo 80% de linhas/70% de branches e anexar o relatório do commit avaliado.
5. Executar JMeter em ambiente PostgreSQL descartável e publicar JTL/HTML sanitizados junto ao resultado do commit final.
6. Obter revisão e aprovação por pares registrada no PR #11 ou em outro PR que contenha o commit exato.
7. Atualizar o relatório SonarCloud e o relatório final de qualidade para o HEAD `080251a`; não declarar aprovação da tag até essa revisão.
8. Confirmar assinatura/votação, nomes e responsáveis pelos riscos antes de autorizar uma tag ou release.
