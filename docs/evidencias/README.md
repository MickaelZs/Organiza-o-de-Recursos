# Índice do pacote de evidências

## Validação dos fluxos obrigatórios

[Fluxos Chromium](interface-fluxos-2026-11-12.md) e [JMeter B e D](../performance/resultado-01.md) registram a execução com dados sintéticos e seus limites. A edição de cadastros de recursos não está explicitada como requisito no escopo fornecido; alteração de reservas foi testada.


## Complemento do clone e material

No Chromium, ADMIN cadastrou uma sala, um professor e um material sintéticos e os três apareceram nas respectivas listas. SOLICITANTE filtrou as listas por localização, competência e tipo, respectivamente, e encontrou os três registros. Edição de recursos não foi testada: não há endpoint ou controle correspondente.

Clone limpo `3d12b9d`: Java 21, 215 testes e RTM estrutural aprovados. Em banco novo `qa_clone_final_20261112`, bootstrap interativo do ADMIN terminou com sucesso; aplicação iniciada com Maven Wrapper em porta alternativa, health HTTP 200 e login/logout visual ADMIN aprovados. [Amostras JMeter sanitizadas](jmeter-2026-11-12/) incluem execução de material e os resultados anteriores; [relatório](../performance/resultado-01.md).


## Correção de contenção — evidências locais

215 testes aprovados com Java 21 e gates JaCoCo aprovados; RTM estrutural válida. A reprodução e os resultados JMeter estão em [resultado 01](../performance/resultado-01.md). Os arquivos temporários de execução não são artifacts duráveis. Os checks do commit final e o clone operacional ainda precisam ser verificados.


## Auditoria adicional de 12/11/2026

- [Regressão anterior e health check](etapa-6-2026-11-12.md). Clone limpo do commit 33bf864: 214 testes sem falhas, erros ou ignorados, gates JaCoCo e RTM estrutural aprovados. Bootstrap e login no clone não foram concluídos.
- Chromium headless: login e logout dos três perfis sintéticos; navegação pelas abas; larguras 320, 375, 390, 768, 1024 e 1440 px. Após ajuste CSS servido localmente ao navegador, não houve transbordamento horizontal da página nas medições. Uma mensagem de erro interno apareceu para ADMIN durante carga concorrente; fluxos operacionais completos não foram validados.
- [JMeter atual](../performance/resultado-01.md): quatro respostas 500 sob carga; investigação e repetição pendentes.
- [RTM](../rtm.md), [defeitos](../defeitos/registro-defeitos.md), [relatório de qualidade](../entrega/relatorio-final-qualidade.md), [CI histórica](https://github.com/MickaelZs/Organiza-o-de-Recursos/actions/runs/43265545752) e [Sonar histórico](../entrega/sonarcloud.md). Os resultados históricos não comprovam o commit final.

O conteúdo seguinte é histórico e mantém os SHAs de sua execução.


## Identificação do commit

- Repositório: [MickaelZs/Organiza-o-de-Recursos](https://github.com/MickaelZs/Organiza-o-de-Recursos)
- Branch avaliada: feature/qa-finalizacao-etapas-1-2.
- Commit avaliado: 3426e518d9993c5db54de088d30773fb60ac1f40.
- Pull request: [#9](https://github.com/MickaelZs/Organiza-o-de-Recursos/pull/9), aberto.
- Estado: as evidências abaixo pertencem ao PR/branch. Ainda não há commit em main, tag ou decisão assinada.

PASS é limitado ao escopo declarado. PARCIAL e PENDENTE indicam uma lacuna;
um arquivo ou link sozinho não prova execução.

## Build, testes e clone limpo

**TST-001 — PASS**

- Clone shallow temporário, HEAD conferido no SHA acima.
- Java 21.0.11, Docker Engine 29.9.2, Testcontainers com PostgreSQL 16.15.
- clean verify: 214 testes, 0 falhas, 0 erros, 0 ignorados; JaCoCo gates aprovados.
- ValidateRtm.java: 24 IDs e 174 links locais, validação aprovada.
- Evidência reproduzível e resultados: [relatório de testes](../testes/relatorio-testes.md), [checklist](../entrega/checklist.md).
- Limite: servidor, bootstrap interativo e login manual não foram executados no clone.

## GitHub Actions e artifact

**CI-001 — PASS para o commit do PR**

- [Workflow run 43265545752](https://github.com/MickaelZs/Organiza-o-de-Recursos/actions/runs/43265545752)
- Build: SUCCESS.
- Artifact: test-reports, contém relatórios Surefire/Failsafe e JaCoCo.
- JaCoCo no artifact: LINE 89,84% (1.070/1.191); BRANCH 72,07% (320/444).
- Limite: run está associado à branch do PR, ainda não a main ou tag.

## SonarCloud

**SON-001 — PASS para o commit do PR**

- [Check/Quality Gate do PR #9](https://sonarcloud.io/dashboard?id=MickaelZs_Organiza-o-de-Recursos&pullRequest=9)
- Quality Gate: OK; Reliability A; Security A; Maintainability A.
- Bugs novos: 0; duplicação nova: 0%; hotspots revisados: 100%.
- Consulta de issues abertas no código novo: total 0.
- Detalhes: [relatório SonarCloud](../entrega/sonarcloud.md).

## Requisitos, arquitetura e testes

**RTM-001 / ARC-001 / SEC-001 — PASS no escopo automatizado/documental**

- [RTM](../rtm.md), [status resumido](../status-requisitos.md), [diagramas](../diagramas/arquitetura.md), [ADRs](../adr/), [matriz de autorização](../seguranca/matriz-autorizacao.md).
- Testes de autorização, bootstrap, reserva, manutenção, auditoria, notificação, relatórios e concorrência foram executados na suíte acima.
- Limites: sem automação de navegador; inspeção visual manual pendente.

## Concorrência e persistência

**CON-001 — PASS para casos automatizados**

- [ReservaConcorrenciaPostgresTest](../../src/test/java/br/com/senac/organizacaorecursos/ReservaConcorrenciaPostgresTest.java)
- [ManutencaoReservaConcorrenciaPostgresTest](../../src/test/java/br/com/senac/organizacaorecursos/ManutencaoReservaConcorrenciaPostgresTest.java)
- Os testes sincronizam chamadas concorrentes e verificam reservas/manutenções persistidas no PostgreSQL.
- Não é evidência de capacidade de produção e não substitui JMeter.

## JMeter

**JMT-001 — PARCIAL**

- [Plano de carga](../performance/plano-jmeter.md) e planos .jmx versionados.
- [Resultado 01 desta auditoria](../performance/resultado-01.md) registra que JMeter não estava instalado e JTL/HTML antigos não estavam disponíveis.
- [Relatório histórico](../performance/resultado-jmeter.md) é preservado, mas seus valores não foram atribuídos ao commit atual.
- Pendência: nova execução em ambiente descartável com JTL/HTML sanitizados.

## Interface e clone operacional

**UI-001 / CLN-001 — PARCIAL**

- [Teste de interface/API](../../src/test/java/br/com/senac/organizacaorecursos/InterfaceErrosIntegrationTest.java).
- Os assets e contratos de erro são cobertos por teste; nenhum screenshot ou fluxo autenticado de navegador foi capturado.
- O clone reproduziu clean verify; bootstrap interativo, startup manual e login não foram testados.

## Defeitos

**BUG-001 — corrigidos**

- D-001 a D-004: histórico de defeitos funcionais/documentais corrigidos.
- D-005/D-006: achados Sonar do bootstrap e da auditoria de conflito corrigidos no commit 3426e51.
- [Registro de defeitos](../defeitos/registro-defeitos.md).

## Decisão e autoria

**DEC-001 — PENDENTE**

- [Decisão de qualidade](../decisao-qualidade.md) é rascunho técnico, não assinado.
- O PR não tem reviews registrados.
- Commits da branch mostram guimaraesjamespotter07-del e arthuralvesx; nomes completos, composição do grupo, contribuições declaradas, votação e riscos aceitos dependem de confirmação real.

## Release candidate

**RC-001 — NÃO PREPARADO PARA PUBLICAÇÃO**

- [Checklist da entrega](../entrega/checklist.md) e [CHANGELOG](../../CHANGELOG.md) estão preparados como drafts.
- A tag v1.0.0-rc1 deve apontar para o commit avaliado em main com CI verde. O PR está aberto; nenhuma tag foi criada ou movida.
- Relatórios locais em target/ não fazem parte do pacote GitHub. O artifact CI é a evidência de cobertura versionada remotamente.
