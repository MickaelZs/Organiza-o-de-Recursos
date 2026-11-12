# Pacote de evidências — Etapa 5

Índice das evidências verificadas nesta retomada. O estado de referência é a branch `feature/qa-finalizacao-etapas-1-2`, HEAD `f58a68e`, com alterações ainda não commitadas na árvore de trabalho. Nenhum token, senha, hash ou payload privado é incluído neste índice.

`PASS` significa somente que o artefato e o resultado descrito foram comprovados no escopo indicado. `PARCIAL`, `PENDENTE` e `BLOQUEADA` mantêm lacunas explícitas; a existência de um nome de arquivo não é tratada como prova de execução.

Identificação Git usada neste índice: `f58a68e` é o HEAD atual da branch `feature/qa-finalizacao-etapas-1-2` (09/11/2026, `feat: finalize admin bootstrap and quality gates`), com alterações posteriores ainda não commitadas. `d0100a1` é a execução remota anterior em `main` (08/11/2026) e não representa esta árvore. Os commits `7948ce7` (04/11/2026, testes RED de manutenção) e `6a758d2` (04/11/2026, implementação de manutenção) são referências históricas de TDD, não o HEAD da auditoria.

Os marcos quantitativos anteriores — 192 testes/93,03% LINE/76,44% BRANCH em `d0100a1` e 204 testes/87,78% LINE/70,09% BRANCH em `f58a68e` — estão preservados no [relatório de testes](../testes/relatorio-testes.md) com suas versões e datas de registro. O resultado vigente da árvore de trabalho é 214 testes/89,22% LINE/72,07% BRANCH em 12/11/2026.

## 1. Requisitos e RTM

**RTM-001**

- Requisito relacionado: RF-01–RF-14, RC-01–RC-06 e RNF-01–RNF-04.
- Origem: RTM versionada e especificação oficial do projeto.
- Data: 12/11/2026.
- Commit ou branch: `feature/qa-finalizacao-etapas-1-2` / `f58a68e` + árvore de trabalho.
- Resultado: matriz revisada; `java scripts/ValidateRtm.java` retornou `RTM structural validation passed` após a suíte completa.
- Caminho ou URL: [docs/rtm.md](../rtm.md), [especificação oficial](https://afonsolelis.github.io/aulas_senac/pages/qualidade2/especificacao-projeto.html), [scripts/ValidateRtm.java](../../scripts/ValidateRtm.java).
- Status: PASS local estrutural.
- Limitações: a RTM não substitui testes comportamentais; CI/SonarCloud, clone limpo e inspeção visual permanecem externos.

## 2. Arquitetura e ADRs

**ARC-001**

- Requisito relacionado: RF-01, RF-05–RF-10, RNF-02 e entregáveis arquiteturais.
- Origem: código Spring Boot, diagramas Mermaid e ADRs versionados.
- Data: 12/11/2026.
- Commit ou branch: `feature/qa-finalizacao-etapas-1-2` / `f58a68e` + árvore de trabalho.
- Resultado: arquitetura em camadas, contexto, componentes, sequência de concorrência, modelo arquitetural e decisões específicas preservados; ADR-0005 documenta não comparecimento.
- Caminho ou URL: [arquitetura](../arquitetura.md), [diagramas](../diagramas/arquitetura.md), [ADRs](../adr/).
- Status: PASS local documental.
- Limitações: não há imagem renderizada nem revisão por pares comprovada nesta árvore.

## 3. Testes unitários e parametrizados

**TST-001**

- Requisito relacionado: RNF-02 e regras de validação, JWT, BCrypt e estados.
- Origem: JUnit 5/Jupiter, testes unitários e `@ParameterizedTest`.
- Data: 12/11/2026.
- Commit ou branch: `feature/qa-finalizacao-etapas-1-2` / `f58a68e` + árvore de trabalho.
- Resultado: a suíte completa executada em 12/11/2026 passou com 214 testes, 0 falhas, 0 erros e 0 ignorados.
- Caminho ou URL: [plano de testes](../testes/plano-testes.md), [relatório de testes](../testes/relatorio-testes.md), [src/test/java](../../src/test/java/).
- Status: PASS local.
- Limitações: não há automação de navegador E2E; o material bruto em `target/` não é pacote portável.

## 4. Testes de integração e Testcontainers

**INT-001**

- Requisito relacionado: RF-02–RF-12, RC-01–RC-06 e RNF-02.
- Origem: classes `*PostgresTest`, migrations Flyway e PostgreSQL real via Testcontainers.
- Data: 12/11/2026.
- Commit ou branch: `feature/qa-finalizacao-etapas-1-2` / `f58a68e` + árvore de trabalho.
- Resultado: 214 testes aprovados, 0 falhas, 0 erros e 0 ignorados com PostgreSQL 16.15 via Testcontainers. A execução local também registrou as 10 migrations do Flyway; o log bruto de inicialização não é versionado.
- Caminho ou URL: [relatório de testes](../testes/relatorio-testes.md), [pom.xml](../../pom.xml), [testes PostgreSQL](../../src/test/java/).
- Status: PASS local.
- Limitações: os relatórios `target/` são locais e não são pacote portável.

## 5. Segurança e autorização

**SEC-001**

- Requisito relacionado: RF-01, RF-07, RF-09, RF-10, RF-11 e RNF-04.
- Origem: `SecurityConfig`, filtro JWT, serviços de ownership e testes de autorização.
- Data: 12/11/2026.
- Commit ou branch: `feature/qa-finalizacao-etapas-1-2` / `f58a68e` + árvore de trabalho.
- Resultado: regras 401/403, perfis, ownership e proteção de dados estão documentadas e cobertas pela suíte PostgreSQL local aprovada.
- Caminho ou URL: [matriz de autorização](../seguranca/matriz-autorizacao.md), [SecurityConfig](../../src/main/java/br/com/senac/organizacaorecursos/config/SecurityConfig.java), [testes](../../src/test/java/br/com/senac/organizacaorecursos/SecurityAuthorizationIntegrationTest.java).
- Status: PASS local.
- Limitações: SonarCloud e execução remota não foram comprovados para esta árvore.

## 6. Concorrência e dupla reserva

**CON-001**

- Requisito relacionado: RF-06, RC-02, RC-03 e RC-04.
- Origem: locks pessimistas nos repositórios, serviços transacionais e testes concorrentes.
- Data: 12/11/2026.
- Commit ou branch: `feature/qa-finalizacao-etapas-1-2` / `f58a68e` + árvore de trabalho.
- Resultado: implementação e testes concorrentes foram executados e aprovados na suíte PostgreSQL de 12/11/2026.
- Caminho ou URL: [ADR-0003](../adr/adr-0003-protecao-concorrencia-reservas.md), [ReservaConcorrenciaPostgresTest](../../src/test/java/br/com/senac/organizacaorecursos/ReservaConcorrenciaPostgresTest.java), [ManutencaoReservaConcorrenciaPostgresTest](../../src/test/java/br/com/senac/organizacaorecursos/ManutencaoReservaConcorrenciaPostgresTest.java).
- Status: PASS local.
- Limitações: JMeter e testes de concorrência são evidências diferentes; nenhum deles prova capacidade de produção.

## 7. JMeter e desempenho

**JMT-001**

- Requisito relacionado: RNF-04 e desempenho da consulta de disponibilidade.
- Origem: execução JMeter 5.6.3 versionada em relatório agregado.
- Data: 08/11/2026.
- Commit ou branch: `feature/qa-finalizacao-etapas-1-2` / `f58a68e` + árvore de trabalho.
- Resultado: 100 amostras, 100 HTTP 200 e 0 erros em `GET /api/salas?tipo=Aula`; o JTL/HTML local foi inspecionado, mas não é transportado pelo repositório.
- Caminho ou URL: [resultado JMeter](../performance/resultado-jmeter.md), [plano de consulta](../performance/organizacao-recursos-consulta.jmx).
- Status: PASS local para o cenário registrado.
- Limitações: JTL/HTML estão em `target/` e não acompanham o Git; não é uma conclusão de capacidade.

**JMT-002**

- Requisito relacionado: RF-05, RF-06, RF-08 e RNF-04.
- Origem: JTLs locais dos cenários A–F do plano de carga.
- Data: 10/11/2026.
- Commit ou branch: `feature/qa-finalizacao-etapas-1-2` / `f58a68e` + árvore de trabalho.
- Resultado: execuções finais A-2, B, C, C-professor, D, E-2 e F terminaram com assertions aprovadas; execuções preliminares tiveram 401 e o cenário combinado C/E/F teve HTTP 500/timeouts.
- Caminho ou URL: [plano A–F](../performance/organizacao-recursos-carga.jmx), [relatório agregado](../performance/resultado-jmeter.md).
- Status: PARCIAL.
- Limitações: JTLs brutos estão ignorados em `target/`; arquivos locais de token não devem ser compartilhados e não são referenciados aqui.

## 8. JaCoCo

**COV-001**

- Requisito relacionado: RNF-01.
- Origem: `jacoco.xml`, `jacoco.csv` e gate configurado no Maven.
- Data: 12/11/2026.
- Commit ou branch: `feature/qa-finalizacao-etapas-1-2` / `f58a68e` + árvore de trabalho.
- Resultado: LINE 89,22% (1.092/1.224) e BRANCH 72,07% (320/444); `jacoco-check` aprovado nessa execução.
- Caminho ou URL: [pom.xml](../../pom.xml), [relatório de testes](../testes/relatorio-testes.md).
- Status: PASS local.
- Limitações: `target/` não é evidência versionada; os percentuais são desta execução local.

## 9. GitHub Actions

**CI-001**

- Requisito relacionado: RNF-03.
- Origem: workflow versionado.
- Data: 12/11/2026.
- Commit ou branch: `feature/qa-finalizacao-etapas-1-2` / `f58a68e` + árvore de trabalho.
- Resultado: workflow declara execução em push/PR, testes, JaCoCo e artifacts; não há execução remota comprovada para esta árvore não publicada.
- Caminho ou URL: [workflow CI](../../.github/workflows/ci.yml), [relatório de testes](../testes/relatorio-testes.md).
- Status: PENDENTE.
- Limitações: YAML não prova que uma execução remota ocorreu nem que terminou verde.

## 10. SonarCloud

**SON-001**

- Requisito relacionado: RNF-03.
- Origem: configuração condicional do workflow e relatório de status.
- Data: 12/11/2026.
- Commit ou branch: `feature/qa-finalizacao-etapas-1-2` / `f58a68e` + árvore de trabalho.
- Resultado: integração configurada; token, organização, projeto e Quality Gate desta árvore não foram comprovados.
- Caminho ou URL: [status SonarCloud](../entrega/sonarcloud.md), [workflow](../../.github/workflows/ci.yml).
- Status: PENDENTE.
- Limitações: não há link remoto atribuível ao HEAD/árvore atual.

## 11. Auditoria e relatórios

**AUD-001**

- Requisito relacionado: RF-10, RF-12, RC-06 e relatório final de qualidade.
- Origem: serviços, migrations, testes PostgreSQL e documentação.
- Data: 12/11/2026.
- Commit ou branch: `feature/qa-finalizacao-etapas-1-2` / `f58a68e` + árvore de trabalho.
- Resultado: auditoria estruturada, não comparecimento, relatórios por recurso/professor e registro de defeitos estão documentados; os fluxos correspondentes possuem testes locais aprovados.
- Caminho ou URL: [teste de auditoria](../../src/test/java/br/com/senac/organizacaorecursos/AuditoriaPostgresTest.java), [teste de notificações](../../src/test/java/br/com/senac/organizacaorecursos/NotificacaoPostgresTest.java), [teste de relatórios](../../src/test/java/br/com/senac/organizacaorecursos/RelatorioPostgresTest.java), [registro de defeitos](../defeitos/registro-defeitos.md), [API de relatórios](../api-relatorios.md), [relatório final](../entrega/relatorio-final-qualidade.md).
- Caminho complementar: [catálogo de API e fluxos](../api-fluxos.md).
- Status: PASS local documental e funcional.
- Limitações: auditoria documental não substitui reexecução dos testes de banco.

## 12. Frontend e fluxos E2E

**UI-001**

- Requisito relacionado: RF-13 e fluxos públicos de login, navegação e erros.
- Origem: frontend estático servido pelo Spring Boot e teste de integração da interface.
- Data: 12/11/2026.
- Commit ou branch: `feature/qa-finalizacao-etapas-1-2` / `f58a68e` + árvore de trabalho.
- Resultado: assets, redirect do Swagger e contratos JSON têm teste de integração; não há automação de navegador nem inspeção visual registrada.
- Caminho ou URL: [frontend](../../src/main/resources/static/), [InterfaceErrosIntegrationTest](../../src/test/java/br/com/senac/organizacaorecursos/InterfaceErrosIntegrationTest.java), [contrato de erros](../api-erros.md).
- Status: PARCIAL.
- Limitações: responsividade visual em resoluções/dispositivos ainda não foi comprovada.

## 13. Autoria e contribuição dos integrantes

**AUT-001**

- Requisito relacionado: entregável de autoria, histórico de trabalho e revisão por pares.
- Origem: histórico Git local e branches do repositório.
- Data: 12/11/2026.
- Commit ou branch: `feature/qa-finalizacao-etapas-1-2` / `f58a68e` + histórico local.
- Resultado: há histórico de branches e commits de desenvolvimento; não foi encontrado quadro versionado atribuindo cada contribuição aos quatro integrantes.
- Caminho ou URL: [histórico Git](../../.git/), [guia de prova oral](../prova-oral/guia.md).
- Status: PENDENTE.
- Limitações: nomes e atribuições não serão inferidos de e-mails/commits sem validação do grupo.

## Limitações gerais

- O pacote não contém prints fictícios, credenciais, tokens, hashes ou links remotos não comprovados.
- `target/`, JTLs, HTML do JMeter, Surefire e JaCoCo são artefatos locais ignorados pelo Git; os relatórios versionados registram apenas métricas verificadas e limitações.
- A decisão global desta etapa permanece `PARTIAL` por CI/SonarCloud não comprovados para a árvore atual, inspeção visual, clone limpo e autoria versionada pendentes; a suíte local e os gates JaCoCo estão aprovados.

## Auditoria de comprovação — 12/11/2026

- Os 27 relatórios XML atuais em `target/surefire-reports/` totalizam 214 testes, sem falhas, erros ou ignorados; essa é a base local para as declarações de testes aprovados.
- `target/site/jacoco/jacoco.xml` foi conferido contra o relatório: LINE 1.092/1.224 e BRANCH 320/444. O gate Maven permanece uma evidência local, não uma aprovação remota.
- Os JTL/HTML JMeter foram conferidos localmente para os cenários descritos, mas permanecem ignorados em `target/`; por isso os resultados JMeter são locais e não reproduzíveis apenas pelo clone.
- Não foram tratados como comprovados: CI/SonarCloud do HEAD atual, inspeção visual, clone limpo, autoria dos quatro integrantes e uma sessão manual de boot/login sem log versionado.
