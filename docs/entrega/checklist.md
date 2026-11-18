# Checklist de entrega — v1.0.0-rc1

| Item | Existe | Atualizado | Caminho |
|---|---|---|---|
| Repositório público com revisão por pares | Parcial | PR #12 integrado pela equipe; revisões individuais não aparecem no GitHub | [PR #12](https://github.com/MickaelZs/Organiza-o-de-Recursos/pull/12) |
| Aplicação executável | Parcial | Projeto buildou em clone local e na CI; bootstrap/login não tiveram novo artifact final | [pom.xml](../../pom.xml), [src/main/java/](../../src/main/java/) |
| README | Sim | Presente na `main` integrada | [README.md](../../README.md) |
| Documentação da API | Sim | Presente na `main` integrada | [API fluxos](../api-fluxos.md), [API recursos](../api-recursos.md), [API relatórios](../api-relatorios.md), [API erros](../api-erros.md) |
| RTM | Sim | Presente; exigir vínculo definitivo ao commit da tag no pacote final | [rtm.md](../rtm.md) |
| Diagramas de contexto | Sim | Presente | [arquitetura dos diagramas](../diagramas/arquitetura.md) |
| Diagramas de componentes | Sim | Presente | [arquitetura dos diagramas](../diagramas/arquitetura.md) |
| Diagramas de sequências críticas | Sim | Presente | [arquitetura dos diagramas](../diagramas/arquitetura.md) |
| Plano de testes | Sim | Presente | [plano de testes](../testes/plano-testes.md) |
| Relatório de testes | Sim | Atualizado historicamente; build atual 215 testes aprovados | [relatório de testes](../testes/relatorio-testes.md) |
| Pipeline de CI | Sim | CI pós-merge verde no commit 58d9183 | [workflow](../../.github/workflows/ci.yml), [execução](https://github.com/MickaelZs/Organiza-o-de-Recursos/actions/runs/44409108743) |
| Relatório SonarCloud | Sim | Quality Gate aprovado no PR #12 | [SonarCloud](https://sonarcloud.io/dashboard?id=MickaelZs_Organiza-o-de-Recursos&pullRequest=12) |
| Relatório JaCoCo | Sim | Artifact da CI: LINE 89,84%, BRANCH 72,07% | [índice de evidências](../evidencias/README.md) |
| Plano de teste de carga | Sim | Presente | [plano JMeter](../performance/plano-jmeter.md) |
| Resultado de teste de carga | Parcial | Resultados históricos presentes; execução final integrada não consolidada | [resultado JMeter](../performance/resultado-01.md) |
| Registro de defeitos | Sim | Presente | [registro de defeitos](../defeitos/registro-defeitos.md) |
| Relatório final de qualidade | Sim | Presente e reconciliado com decisão de release candidate | [relatório de qualidade](../relatorio-qualidade.md) |

## Resultado observado

1. O PR #12 foi criado e integrado em `main`.
2. A CI pós-merge do commit `58d91835400f4539b7b98f3236e0db10e19896a0` terminou com sucesso.
3. A verificação local anterior ao merge terminou com `BUILD SUCCESS`: 215 testes, 0 falhas, 0 erros e 0 ignorados.
4. JaCoCo aprovado no artifact: LINE 89,84%, BRANCH 72,07%.
5. SonarCloud passou no PR #12.

## Bloqueadores e ressalvas

1. Consolidar JMeter final e sanitizado contra o commit da tag.
2. Confirmar nomes civis completos e declaração individual de contribuição.
3. Registrar a revisão formal por pares no GitHub.
4. Validar bootstrap/login em clone final e anexar evidência do artifact.
5. Se houver correção posterior, criar `v1.0.0-rc2`; não mover a tag `v1.0.0-rc1`.
