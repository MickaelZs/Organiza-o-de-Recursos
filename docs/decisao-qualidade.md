# Decisão de qualidade

## Identificação

- Projeto: Organização de Recursos.
- Repositório público: https://github.com/MickaelZs/Organiza-o-de-Recursos
- Commit avaliado em `main`: `080251a54165ed16e0a4c73a1e6be2bb9d3e54ef`
- CI avaliada em `main`: [GitHub Actions 43345688930](https://github.com/MickaelZs/Organiza-o-de-Recursos/actions/runs/43345688930)
- PR integrado: [PR #11](https://github.com/MickaelZs/Organiza-o-de-Recursos/pull/11)
- Estado da tag: não existe `v1.0.0-rc1`; não houve envio de tag.
- Data da decisão: 18/11/2026.

## Decisão

**BLOQUEAR o release candidate e qualquer declaração de prontidão para produção.**

O resultado é **liberar somente após correção dos bloqueadores**. O sistema possui evidências positivas de build automatizado, testes, cobertura, documentação e CI em `main`, mas a avaliação externa não conseguiu reproduzir o `verify` por indisponibilidade do Docker, não produziu JAR/JaCoCo, não iniciou o servidor nem validou bootstrap/login, e o JMeter não foi reproduzido integralmente neste ciclo. Não há revisão por pares registrada nem declaração coletiva de riscos aceitos.

Esta decisão não significa que o software esteja pronto para produção. Ela significa que a entrega acadêmica não pode ser considerada aprovada até que as lacunas do checklist sejam resolvidas e verificadas no commit exato.

## Critérios usados

| Critério | Evidência observada | Resultado |
|---|---|---|
| Build reproduzível | `./mvnw -B verify` tentou rodar em clone temporário novo com Java 21.0.11; falhou antes da validação completa porque o Docker daemon não estava disponível. Foram encontrados 93 testes executados, 14 erros de Testcontainers, 0 falhas e 9 ignorados. | Bloqueado |
| CI em `main` | Workflow presente em [.github/workflows/ci.yml](../.github/workflows/ci.yml); a execução remota do commit `080251a...` terminou com sucesso. | Pass remoto |
| Aplicação executável | Projeto Spring Boot presente, README e JAR/estrutura versionados historicamente, mas o ciclo desta avaliação não iniciou PostgreSQL, bootstrap, servidor ou login e não produziu JAR novo. | Bloqueado |
| README e API | [README.md](../README.md), documentação em [docs/api-fluxos.md](api-fluxos.md), [docs/api-recursos.md](api-recursos.md), [docs/api-relatorios.md](api-relatorios.md) e [docs/api-erros.md](api-erros.md). | Presente |
| RTM | [docs/rtm.md](rtm.md) está presente e já registra RF-01–RF-14, RC-01–RC-06 e RNF-01–RNF-04. A validação nesta execução não pôde concluir por falha dos testes. | Não atualizado nesta execução |
| Arquitetura | [Contexto, componentes e sequência](diagramas/arquitetura.md) estão presentes no HEAD. | Presente |
| Plano e relatório de testes | [Plano](testes/plano-testes.md) e [relatório](testes/relatorio-testes.md) existem, mas o relatório é histórico e não representa o resultado externo do commit final. | Presente; resultado não atualizado |
| JaCoCo | O relatório remoto/histórico passa os limites de 80% LINE e 70% BRANCH, mas o diretório `target/site/jacoco/` não foi produzido no clone externo desta avaliação. | Bloqueado no commit atual |
| SonarCloud | O relatório de [SonarCloud](entrega/sonarcloud.md) documenta resultados do PR #9/commit `3426e51`; PR #11 e `080251a` possuem checks separados, mas o documento não é específico desse HEAD. | Parcial |
| JMeter e carga | [Plano](performance/plano-jmeter.md) e [resultado](performance/resultado-01.md) existem. Há execuções isoladas históricas sem 500, mas não é uma execução atual completa e reproduzível do pacote. | Parcial |
| Revisão por pares | PR #10 e PR #11 têm checks aprovados, mas o GitHub não registra `reviewDecision` nem reviews no PR #11. | Bloqueado |
| Defeitos | [Registro de defeitos](defeitos/registro-defeitos.md) existe e separa defeitos corrigidos de riscos/pendências. | Presente |
| Relatório final | [docs/relatorio-qualidade.md](relatorio-qualidade.md) existe, mas contém resultados anteriores e explicitamente deixa pendências humanas e externas. | Presente; não substitui a decisão atual |
| Gestão da entrega | [Checklist](entrega/checklist.md) registra os bloqueadores em ordem e não declara aprovação humana. | Bloqueado |

## Riscos

Não há riscos formalmente aceitos pelos integrantes. Até existir declaração coletiva assinada, qualquer uso além de avaliação técnica fica fora do escopo aprovado.

| Risco | Impacto | Justificativa para bloquear | Responsável |
|---|---|---|---|
| Testes Testcontainers não reproduzidos externamente | Não há garantia atual de que PostgreSQL, concorrência e persistência passam no commit avaliado | Docker indisponível impediu o `verify` externo e a geração de JaCoCo. | Responsável técnico pelo ambiente de CI/testes |
| Aplicação não iniciada nesta avaliação | Bootstrap, login, migrações Flyway e operação HTTP não foram validados contra o clone externo | A execução do README não chegou ao servidor por falta de Docker/PostgreSQL. | Responsável pela implantação e testes funcionais |
| Cobertura não regenerada no commit atual | Não há evidência nova de que os gates 80/70 continuam aprovados no `080251a` | O histórico possui valores positivos, mas não substitui o artifact do HEAD avaliado. | Responsável pela pipeline e cobertura |
| JMeter sem execução atual completa | Latência, vazão e comportamento sob carga não têm uma conclusão durável para o HEAD | Resultado histórico contém cenários isolados e limitações; não substitui carga integrada atual. | Responsável por performance |
| Revisão por pares ausente | A alteração pode conter erros não vistos por outra pessoa | PR #11 não registra revisão ou aprovação no GitHub. | Responsável pelo repositório e reviewers |
| SonarCloud não reconciliado ao HEAD | Análise positiva anterior não prova que o HEAD final permanece aprovado | Documento existente é de PR #9/`3426e51`, não do release candidate. | Responsável por qualidade estática |
| Integração operacional parcial | Problemas de instalação ou primeiro acesso podem surgir fora da suíte unitária | Bootstrap, health e login não foram validados nesta rodada. | Responsável técnico pela implantação |

Os riscos acima não foram aceitos. Os responsáveis indicados são cargos funcionais necessários; a equipe deve designar pessoas nominalmente antes da aprovação.

## Evidência dos defeitos

O [registro de defeitos](defeitos/registro-defeitos.md) contém ocorrências anteriores resolvidas, incluindo cancelamento indevido, auditoria, documentação de disponibilidade e achados Sonar. O achado de carga JMeter segue aberto/pendente de execução completa. A existência de correções não elimina o bloqueio atual: evidências antigas não devem ser atribuídas ao HEAD sem execução verificável.

## Critérios para reconsiderar

1. Disponibilizar Docker/PostgreSQL e obter `./mvnw -B clean verify` verde no commit exato.
2. Gerar JAR, iniciar a aplicação e validar health, bootstrap do primeiro ADMIN e login.
3. Regenerar JaCoCo e anexar os relatórios ao artifact da CI.
4. Executar JMeter em banco descartável e preservar JTL/HTML sanitizados ligados ao commit.
5. Atualizar SonarCloud e o relatório de qualidade para o HEAD final.
6. Obter revisão e aprovação por pares registrada no GitHub.
7. Confirmar os integrantes, suas declarações e responsáveis pelos riscos.
8. Somente então autorizar `v1.0.0-rc1` no commit em `main`.

## Autoria e declaração do trabalho

O Git é a única fonte disponível para esta seção. Os nomes abaixo são identidades de autor observáveis; não são declaração individual confirmada pelo formulário ou por assinatura.

| Pessoa/identidade Git | O que o histórico indica |
|---|---|
| Mickael Vinicius / MickaelZs | Integrações de pull requests, correções finais de reservas, CI, documentação e consolidação de evidências. |
| guimaraesjamespotter07-del | Commits de implementação de bootstrap e documentação/qualidade. |
| arthuralvesx / Arthur Alves Barbosa | Correções SonarCloud, interface responsiva, auditoria da Etapa 6 e ajustes finais visuais/documentais. |
| Nabil Saba | Auditoria de contenção, planos JMeter, responsividade, evidências de clone e documentação de instalação/validação. |

Os nomes civis completos e a divisão individual declarada pelos integrantes não foram fornecidos. Esta tabela não presume que todos concordaram com a decisão. O campo de autoria do formulário deve ser preenchido pelos próprios integrantes, informando o que cada um realizou.

## Assinaturas e aceite

- Responsável pela decisão: não definido nominalmente.
- Responsáveis pelos riscos: não definidos nominalmente.
- Votação ou assinaturas dos integrantes: não fornecidas.
- Responsável por revisão por pares: não definido.
- Responsável pelo release: não autorizado.

Sem esses registros, não é possível declarar aceitação coletiva dos riscos nem autorizar produção ou publicação de release candidate.

## Próxima decisão

Quando os bloqueadores forem removidos, esta decisão pode ser reconsiderada. Uma possível avaliação posterior deve usar a distinção abaixo:

- **Liberar:** todos os critérios críticos verdes, CI/JaCoCo/Sonar atualizados, operação básica validada, revisão por pares registrada e riscos aceitos nominalmente.
- **Liberar com ressalvas:** critérios não críticos parcialmente faltantes, desde que a equipe registre impacto, mitigação e responsável.
- **Bloquear:** falha em build, operação básica, segurança, revisão por pares ou ausência de aceite dos riscos.

O estado atual é **bloquear**.

## Texto para o formulário de entrega

Repositório público: https://github.com/MickaelZs/Organiza-o-de-Recursos

Commit avaliado em `main`: `080251a54165ed16e0a4c73a1e6be2bb9d3e54ef`

Tag avaliada: nenhuma. `v1.0.0-rc1` não foi criada e não há tag autorizada para esta entrega.

Decisão: bloquear o release candidate até corrigir a reprodução externa do `verify`, gerar o JAR/JaCoCo, concluir a validação operacional e de carga, atualizar SonarCloud/qualidade e obter revisão por pares registrada.
