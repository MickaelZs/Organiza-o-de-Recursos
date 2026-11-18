# Índice do pacote de evidências — v1.0.0-rc1

## Identificação da release candidate

- Repositório público: https://github.com/MickaelZs/Organiza-o-de-Recursos
- Commit integrado avaliado: `58d91835400f4539b7b98f3236e0db10e19896a0`
- PR integrado: [PR #12](https://github.com/MickaelZs/Organiza-o-de-Recursos/pull/12)
- Tag candidata: `v1.0.0-rc1`

## Evidências

- [Execução do CI no commit da integração](https://github.com/MickaelZs/Organiza-o-de-Recursos/actions/runs/44409108743)
- Artifact da CI `test-reports`: Surefire, JaCoCo XML/HTML e relatório do commit integrado.
- [Quality Gate SonarCloud do PR #12](https://sonarcloud.io/dashboard?id=MickaelZs_Organiza-o-de-Recursos&pullRequest=12)
- [Plano e resultado de carga](../performance/resultado-01.md)
- [Plano JMeter](../performance/plano-jmeter.md)
- [Matriz de rastreabilidade](../rtm.md)
- [Registro de defeitos](../defeitos/registro-defeitos.md)
- [Relatório de qualidade](../relatorio-qualidade.md)
- [Decisão de qualidade](../decisao-qualidade.md)
- [Checklist da entrega](../entrega/checklist.md)

## Resultado de qualidade

O CI pós-merge terminou com sucesso no commit `58d91835400f4539b7b98f3236e0db10e19896a0`.

JaCoCo do artifact da execução:

- LINE: 89,84% (1.070/1.191).
- BRANCH: 72,07% (320/444).
- Gates requeridos: 80% LINE e 70% BRANCH.
- Resultado: aprovado.

A verificação local desta branch anterior ao merge registrou:

- 215 testes.
- 0 falhas.
- 0 erros.
- 0 ignorados.
- `BUILD SUCCESS`.

O SonarCloud passou no PR #12. A execução de `main` não executou a análise Sonar quando credenciais/variáveis não estavam disponíveis no evento push.

## Limitações

Os cenários JMeter históricos não constituem uma execução durável e completa do commit candidato. Bootstrap e login não foram incorporados a um novo artifact nesta release. O PR #12 não registra revisões individuais no GitHub; a integração foi confirmada pelo merge realizado pela equipe.

Todas as conclusões desta documentação são limitadas ao ambiente automatizado e aos artefatos citados. Não declarar capacidade de produção nem aprovação de carga ampla.
