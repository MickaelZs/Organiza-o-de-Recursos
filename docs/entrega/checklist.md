# Checklist de entrega em clone limpo

## Complemento operacional

Clone `3d12b9d`: bootstrap em banco novo, health, login e logout confirmados; suite e RTM passaram. A edição de cadastros de recursos não é requisito explicitado no escopo fornecido; os fluxos operacionais foram exercitados conforme a evidência de interface. O material foi testado com dez requisições concorrentes; dados sanitizados estão no [índice de evidências](../evidencias/README.md). A decisão de liberação permanece sujeita à revisão humana, aos checks do commit final e à integração em main.


## Correção de contenção — atualização

Teste PostgreSQL com 16 requisições passou e carga combinada repetida teve zero 500. Permanecem pendentes a execução isolada completa dos cenários JMeter, fluxos de navegador, bootstrap em clone limpo, CI/Sonar do commit final e revisão humana. [Resultado](../performance/resultado-01.md).


## Atualização da auditoria de 12/11/2026 — branch feature/qa-entrega-final

| Entregável | Existe | Atualização e evidência | Caminho |
|---|---|---|---|
| Código e API | Sim | Suíte em clone 33bf864 aprovada; fluxo visual ainda incompleto | [README](../../README.md), [API](../api-fluxos.md) |
| RTM, testes e cobertura | Sim | 214 testes e gates aprovados no clone; novo resultado deve acompanhar o commit final | [RTM](../rtm.md), [testes](../testes/relatorio-testes.md) |
| Arquitetura e ATAM | Sim | Revisão do grupo pendente | [diagramas](../diagramas/arquitetura.md), [ATAM](../atam.md) |
| JMeter e defeitos | Sim | Carga executada com 4 respostas 500; bloqueio aberto | [resultado](../performance/resultado-01.md), [defeitos](../defeitos/registro-defeitos.md) |
| CI, Sonar e revisão | Sim | Conferir PR e commit final; aprovação humana pendente | [workflow](../../.github/workflows/ci.yml), [Sonar](sonarcloud.md) |
| Evidências, qualidade e decisão | Sim | Rascunho, sem aprovação dos integrantes | [índice](../evidencias/README.md), [qualidade](relatorio-final-qualidade.md), [decisão](../decisao-qualidade.md) |
| Apresentação e demonstração | Parcial | Roteiro disponível; apresentação e revisão humana pendentes | [guia](../prova-oral/guia.md) |
| Release e formulário | Não | Tag, nomes e aprovação pendentes; não publicar | [decisão](../decisao-qualidade.md) |

As seções históricas abaixo referem-se ao commit e PR nelas identificados, não ao estado atual.


## Escopo e evidência

- Repositório: MickaelZs/Organiza-o-de-Recursos (público).
- Branch avaliada: feature/qa-finalizacao-etapas-1-2.
- Commit avaliado: 3426e518d9993c5db54de088d30773fb60ac1f40.
- Pull request: [#9](https://github.com/MickaelZs/Organiza-o-de-Recursos/pull/9), aberto, sem reviews registrados.
- Clone: diretório temporário novo, clone shallow do GitHub, confirmado no SHA acima.
- Comando: .\mvnw.cmd -B clean verify, seguido por java scripts\ValidateRtm.java.
- Resultado: BUILD SUCCESS; 214 testes, 0 falhas, 0 erros, 0 ignorados; JaCoCo passou; RTM estrutural validada (24 IDs, 177 links locais).
- Runtime: Java 21.0.11, Docker Engine 29.9.2, PostgreSQL 16.15 via Testcontainers.

## Entregáveis

| Item | Existe | Atualizado para 3426e51 | Caminho ou evidência |
|---|---|---|---|
| Repositório público e histórico Git | Sim | Sim | https://github.com/MickaelZs/Organiza-o-de-Recursos |
| Aplicação Java/Spring Boot | Sim | Build reproduzido | src/main/java/ |
| Instruções de instalação e primeiro ADMIN | Sim | URL de clone corrigida; fluxo escrito | README.md, docs/guia-instalacao.md |
| Documentação da API e fluxos | Sim | Revisada no repositório | docs/api-fluxos.md, docs/api-recursos.md, docs/api-relatorios.md, /api-docs |
| RTM RF/RC/RNF | Sim | Status reconciliados nesta auditoria | docs/rtm.md, docs/status-requisitos.md |
| Diagramas de contexto/componentes/sequência | Sim | Presentes; revisão visual humana pendente | docs/diagramas/arquitetura.md |
| ADRs, arquitetura e ATAM/utility tree | Parcial | ADRs/arquitetura existentes; árvore e cenários ATAM preliminares adicionados nesta auditoria, ainda sem validação do grupo | docs/adr/, docs/arquitetura.md, docs/atam.md |
| Plano e relatório de testes | Sim | Resultado atual registrado | docs/testes/plano-testes.md, docs/testes/relatorio-testes.md |
| GitHub Actions | Sim | Build verde para o SHA avaliado | run 43265545752 |
| Artifact de Surefire/JaCoCo | Sim | Baixado e inspecionado | artifact test-reports, run 43265545752 |
| SonarCloud e Quality Gate | Sim | Aprovados para o PR/SHA | docs/entrega/sonarcloud.md |
| JMeter: plano de carga | Sim | Planos presentes | docs/performance/*.jmx |
| JMeter: resultado reproduzível | Parcial | Métricas antigas não reproduzíveis a partir do clone atual | docs/performance/resultado-01.md |
| Registro de defeitos | Sim | Achados Sonar e correções registrados | docs/defeitos/registro-defeitos.md |
| Relatório de qualidade | Sim | Atualizado nesta auditoria | docs/entrega/relatorio-final-qualidade.md |
| Decisão de qualidade assinada pelo grupo | Não | Rascunho técnico; sem assinaturas ou votação presumidas | docs/decisao-qualidade.md |
| CHANGELOG | Sim | Preparado como draft do release candidate | CHANGELOG.md |
| Release candidate em main com CI verde | Não | Bloqueado até revisão e integração humana | Nenhuma tag criada |

## Bloqueadores e pendências em ordem

1. Revisão por pares do PR #9: GitHub não registra reviews; um integrante deve
   revisar e aprovar conforme a prática do grupo.
2. Execução JMeter atual e isolada: JMeter não está instalado e os JTL/HTML
   referidos anteriormente não existem no workspace. Executar em ambiente
   PostgreSQL descartável e preservar relatório sanitizado.
3. Inspeção visual da interface em desktop e celular e teste manual de login/
   fluxos principais. A inspeção do navegador não foi concluída neste ambiente.
4. Revisão humana da reprodutibilidade operacional: o clone reproduziu
   clean verify, mas não executou o servidor, bootstrap interativo e login
   seguindo todas as etapas do README.
5. Identificação do grupo: confirmar nomes completos, contribuição de cada
   integrante, responsável pelos riscos aceitos e assinaturas/votação.
6. Após os itens anteriores: aprovar o PR, integrar o SHA aprovado em main,
   executar CI nessa main e só então autorizar a tag v1.0.0-rc1. Não criar nem
   mover tag nesta etapa.

## Limites do clone

O diretório temporário foi criado do zero e não reutilizou target/ do workspace.
O clone e clean verify provam a compilação, os testes de integração, Testcontainers,
JaCoCo e a RTM estrutural do commit. Não provam configuração de credenciais reais
de PostgreSQL, bootstrap manual, login visual, desempenho de produção ou
aprovação do grupo.
