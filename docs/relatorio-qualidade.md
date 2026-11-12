# Relatório final de qualidade — auditoria da Etapa 6

## Atualização de 12/11/2026

A regressão da correção de concorrência passou com 215 testes sem falhas, erros ou ignorados; JaCoCo LINE 89,84% e BRANCH 72,07%; RTM estrutural aprovada. A carga corrigida não teve 500 nos cenários repetidos; ver [resultado JMeter](performance/resultado-01.md) para os limites e a execução inicialmente inválida por professor inexistente. Fluxos completos no navegador, clone operacional e checks remotos do commit final permanecem pendentes.


## A. Resumo executivo

O núcleo funcional, a suíte automatizada, a cobertura, a CI e o SonarCloud
passaram no commit 3426e518d9993c5db54de088d30773fb60ac1f40, branch
feature/qa-finalizacao-etapas-1-2, PR #9. O clean verify também foi reproduzido
em clone temporário. A entrega final ainda não pode ser declarada aprovada:
faltam review por pares, execução JMeter atual e reproduzível, inspeção visual,
validação manual do bootstrap/login em clone, nomes e decisão assinada do grupo,
e CI sobre o commit integrado em main. Nenhuma tag, merge ou envio de formulário
foi executado.

## B. Estado Git

- Repositório público: MickaelZs/Organiza-o-de-Recursos.
- Branch: feature/qa-finalizacao-etapas-1-2.
- HEAD avaliado: 3426e518d9993c5db54de088d30773fb60ac1f40.
- PR #9: aberto, base main, sem review registrado.
- Working tree antes desta atualização documental: limpa.
- Autores observáveis nos três commits da branch: guimaraesjamespotter07-del
  (dois commits) e arthuralvesx (um commit). Isso não confirma a composição
  completa nem os nomes civis do grupo.

## C. Revisão das etapas anteriores

| Etapa | Evidência | Resultado |
|---|---|---|
| 1 — primeiro ADMIN | AdminBootstrapServicePostgresTest, AdminBootstrapCommandTest, BCrypt, validação de banco vazio e lock advisory no serviço | PASS automatizado; sessão manual ainda não executada |
| 2 — JaCoCo/CI/SonarCloud | JaCoCo 80/70, build GitHub verde, check SonarCloud verde no commit | PASS |
| 3 — regras, auditoria e relatórios | Testes PostgreSQL de reserva, manutenção, auditoria, notificação e relatórios | PASS automatizado |
| 4 — JMeter/carga | Planos JMX existem; resultados anteriores sem artefatos brutos no workspace | PARCIAL |
| 5 — RTM/documentação/evidências | RTM validada estruturalmente; pacote de evidências atualizado nesta auditoria | PASS documental; sign-off/review pendentes |

## D. Conformidade funcional

RF-01 a RF-12 e RC-01 a RC-06 têm implementação e testes citados na
[RTM](rtm.md), incluindo autenticação, ownership, CRUD de recursos, filtros,
reservas, conflitos, manutenção, aprovação, retirada/devolução, auditoria,
notificações e relatórios. RF-13 permanece parcial até inspeção visual em
desktop e celular. RF-14 está documentado no README, guia, catálogo de API,
fluxos públicos e OpenAPI.

## E. Segurança

Os testes da suíte cobrem JWT, BCrypt, perfis, ownership e respostas 401/403.
O bootstrap limita o alvo a PostgreSQL local, verifica banco vazio antes das
migrations e protege a criação inicial com transação/lock PostgreSQL. A revisão
do diff da correção Sonar não encontrou segredos; uma varredura automatizada
completa do histórico Git não foi executada. O SonarCloud reportou Quality Gate
OK e zero issues abertas no código novo; isso não substitui auditoria de
segredos de todo o histórico nem pentest.

## F. Concorrência

ReservaConcorrenciaPostgresTest e
ManutencaoReservaConcorrenciaPostgresTest executaram no clean verify. Os testes
usam sincronização concorrente e conferem as linhas persistidas no PostgreSQL,
incluindo disputa por recurso e reserva contra manutenção. A evidência confirma
os casos cobertos, não capacidade de produção nem carga ampla.

## G. Testes

214 testes executados, 214 aprovados, 0 falhas, 0 erros e 0 ignorados no clone
limpo e no artifact da CI. Veja [relatório de testes](testes/relatorio-testes.md).
Há unitários, parametrizados, integração e API via MockMvc/PostgreSQL; não há
automação de navegador E2E. A RTM estrutural reportou 24 IDs e 177 links locais.

## H. JaCoCo

- Clone limpo: LINE 90,01% (1.072/1.191); BRANCH 72,07% (320/444).
- Artifact CI: LINE 89,84% (1.070/1.191); BRANCH 72,07% (320/444).
- Limites: 80% LINE e 70% BRANCH.
- Resultado: gates aprovados em ambas as execuções. A diferença de duas linhas
  está registrada sem atribuir causa não comprovada.

## I. JMeter

Há planos de consulta e cenários A–F. A documentação preexistente resume
resultados locais de 08/11 e 10/11, mas os JTL/HTML não estão no workspace atual
nem no artifact CI; JMeter não está instalado. Não foi feita nova execução na
auditoria e os valores históricos não são evidência do commit atual. Consulte
[resultado-01.md](performance/resultado-01.md). A verificação de concorrência
JUnit/PostgreSQL é prova distinta.

## J. Frontend

O projeto tem interface estática servida pelo Spring Boot;
InterfaceErrosIntegrationTest verifica assets, rotas públicas e contrato de
erros. Inspeção visual e fluxos interativos por perfil em navegador não foram
concluídos. A tentativa de obter acesso UI expirou antes da seleção da janela,
então nenhum resultado visual é declarado.

## K. Arquitetura

Monólito Spring Boot em camadas, PostgreSQL, Flyway e API REST documentados em
docs/arquitetura.md. Há diagramas Mermaid de contexto, componentes e sequência
de concorrência, além de ADRs. Uma árvore de utilidade e cenários ATAM
preliminares foram adicionados em [docs/atam.md](atam.md); permanecem sujeitos
à validação do grupo e não registram consenso ou riscos formalmente aceitos.

## L. Documentação

README, guia de instalação, API/fluxos, plano de testes, registro de defeitos,
RTM, documentação arquitetural, prova oral e relatórios estão versionados. O
URL de clone foi corrigido nesta rodada; o checklist da entrega lista existência,
atualização e caminho.

## M. RTM

24 IDs rastreados: RF-01–RF-14, RC-01–RC-06 e RNF-01–RNF-04. A validação
estrutural verificou 177 links locais e encontrou as pendências externas
explicitamente, sem contá-las como arquivos ausentes. RF-13 e RNF-04 continuam
parciais pelas evidências visual/JMeter.

## N. GitHub Actions

Run [43265545752](https://github.com/MickaelZs/Organiza-o-de-Recursos/actions/runs/43265545752)
aprovado para o commit avaliado. O artifact test-reports foi baixado e contém
Surefire, JaCoCo XML/CSV e HTML. A execução corresponde à branch do PR, não a
uma tag ou main integrada.

## O. SonarCloud

Check aprovado no [PR #9](https://sonarcloud.io/dashboard?id=MickaelZs_Organiza-o-de-Recursos&pullRequest=9)
para o commit 3426e51. Quality Gate OK, Reliability A, Security A,
Maintainability A, zero bugs novos, duplicação nova 0%, hotspots revisados
100% e consulta de issues abertas em código novo retornou zero.

## P. Clone limpo

Clone shallow novo da branch, SHA confirmado, Java 21.0.11, Docker Engine
29.9.2, Testcontainers/PostgreSQL 16.15 e clean verify aprovados. A etapa
operacional de iniciar a aplicação seguindo todas as instruções, cadastrar o
ADMIN de forma interativa e fazer login manualmente não foi executada. Assim,
a reprodutibilidade do build está PASS; a reprodução operacional completa é
PARTIAL.

## Q. Evidências

O índice está em [docs/evidencias/README.md](evidencias/README.md). CI, artifact
e SonarCloud têm URLs verificadas. Métricas JMeter históricas são identificadas
como não reproduzíveis; não se anexou saída local ignorada como artifact.

## R. Defeitos e riscos

Defeitos D-001 a D-004 foram corrigidos em etapas anteriores. D-005 e D-006
registram os dois achados Sonar corrigidos no commit 3426e51. Riscos abertos:
JMeter atual, inspeção visual, ausência de review/sign-off e falta de CI/tag em
main. Nenhum bloqueador conhecido de build, autorização automatizada ou
integridade concorrente foi reproduzido na suíte atual.

## S. Autoria

O histórico Git prova autoria de commits por guimaraesjamespotter07-del e
arthuralvesx na branch do PR; o histórico público do repositório também inclui
MickaelZs. Nomes completos, composição final do grupo e declarações individuais
não estão confirmados. Não se presume assinatura nem aprovação.

## T. Release candidate

CHANGELOG e checklist estão preparados como rascunho. A tag v1.0.0-rc1 não foi
criada porque o PR continua aberto e a Aula 14 exige tag no commit avaliado em
main com CI verde. Merge e publicação de tag dependem da revisão e autorização
do responsável.

## U. Decisão de qualidade

O parecer técnico está em [decisao-qualidade.md](decisao-qualidade.md):
parcialmente apto para revisão do PR; bloquear declaração final e tag até
resolver as pendências. Não há riscos aceitos pelo grupo nem assinatura.

## V. Simulação da avaliação

Estimativa de aderência, não nota oficial. Baseada somente nas evidências
observadas; dimensões com pendências recebem intervalo em vez de precisão falsa.

| Dimensão | Peso | Evidência | Aderência estimada |
|---|---:|---|---:|
| Requisitos e regras de negócio | 15% | RF/RC automatizados; inspeção UI pendente | 80–90% |
| Estratégia e documentação de testes | 15% | Plano, RTM, relatório; JMeter sem execução atual | 65–80% |
| Qualidade dos testes automatizados | 25% | 214 testes; PostgreSQL/Testcontainers; gates verdes | 85–95% |
| CI, cobertura e análise estática | 15% | CI/Sonar aprovados; 89,84/72,07 | 95–100% |
| Performance, segurança e robustez | 10% | Segurança/concorrência automatizadas; JMeter pendente | 60–80% |
| Rastreabilidade e gestão de defeitos | 10% | RTM estrutural, registro atualizado, evidência indexada | 80–90% |
| Organização, apresentação e autoria | 10% | documentos e changelog; review, nomes e assinatura pendentes | 50–70% |

Os intervalos são projeções do agente, não nota oficial nem avaliação do professor.

## W. Arquivos preparados nesta auditoria

CHANGELOG.md, docs/decisao-qualidade.md, docs/entrega/checklist.md,
docs/performance/resultado-01.md, docs/relatorio-qualidade.md e documentação
relacionada reconciliada com as evidências atuais.

## X. Pendências humanas e externas

1. Executar/revisar JMeter e a interface visual em ambiente descartável.
2. Executar manualmente bootstrap/login no clone seguindo README.
3. Obter revisão por pares no PR.
4. Confirmar integrantes, contribuições, responsáveis por risco e decisão
   assinada/votada.
5. Depois de satisfeitas as condições, aprovar integração em main e autorizar
   release candidate.
6. Executar CI na main avaliada; somente então criar tag anotada autorizada e
   preencher o formulário por um integrante.

## Y. Parecer final

**PARCIALMENTE APTO.** A branch do PR tem evidências atuais de build, testes
PostgreSQL, cobertura, CI e SonarCloud. Ainda não está pronta para a entrega
final do professor porque faltam carga JMeter reproduzível, inspeção visual,
revisão por pares, decisão assinada do grupo e aprovação/CI em main.
