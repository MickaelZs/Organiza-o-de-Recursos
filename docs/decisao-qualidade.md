# Decisão de qualidade — rascunho para revisão do grupo

## Decisão técnica atual — BLOQUEAR

A carga JMeter de 12/11/2026 produziu quatro HTTP 500; veja [resultado 01](performance/resultado-01.md). Faltam investigação, correção e nova validação, inspeção funcional completa, CI/Sonar no commit final e revisão humana. Não há riscos aceitos nem responsáveis designados. Nomes, contribuições declaradas, aprovação e assinaturas dos integrantes permanecem a confirmar. Nenhuma tag foi autorizada. O histórico abaixo identifica um snapshot anterior.

Texto para formulário (rascunho, não enviar): Repositório público https://github.com/MickaelZs/Organiza-o-de-Recursos; tag avaliada: **pendente de criação e autorização**; integrantes: **nomes a confirmar**.


## Identificação

- Projeto: Organização de Recursos.
- Repositório público: https://github.com/MickaelZs/Organiza-o-de-Recursos
- Branch avaliada: feature/qa-finalizacao-etapas-1-2.
- Commit avaliado nesta auditoria: 3426e518d9993c5db54de088d30773fb60ac1f40.
- Pull request: #9, aberto, sem reviews registrados.
- Data da auditoria: 12/11/2026.

## Parecer técnico provisório

**PARCIALMENTE APTO para revisão do PR; BLOQUEAR a declaração final de entrega
e a criação da tag v1.0.0-rc1 neste momento.**

Build, testes automatizados, JaCoCo, RTM estrutural, CI remota e SonarCloud
foram aprovados no commit identificado. As evidências não cobrem uma execução
JMeter reproduzível neste commit, inspeção visual manual, execução operacional
com bootstrap/login em clone limpo, revisão por pares nem decisão assinada do
grupo. Além disso, o commit está na branch do PR e não na main. A Aula 14
condiciona a tag a CI verde em main, pacote coerente e decisão assinada.

Esta é uma análise técnica não assinada. Não representa votação, aprovação ou
aceitação de risco por qualquer integrante.

## Critérios e evidências

| Critério | Evidência | Resultado |
|---|---|---|
| Requisitos e regras de negócio | RTM RF-01–RF-14 e RC-01–RC-06; testes PostgreSQL | PASS com RF-13 parcialmente verificado visualmente |
| Segurança e autorização | SecurityAuthorizationIntegrationTest, BCrypt/JWT, Sonar sem bugs/vulnerabilidades novos | PASS no escopo automatizado |
| Concorrência e persistência | ReservaConcorrenciaPostgresTest e ManutencaoReservaConcorrenciaPostgresTest, executados no clean verify | PASS; JMeter não substituído por esta evidência |
| Suíte automatizada | Clone limpo, Java 21, Docker/Testcontainers, PostgreSQL 16.15; 214/214, zero falhas/erros/ignorados | PASS |
| Cobertura | CI artifact: 89,84% LINE, 72,07% BRANCH; gate 80/70 | PASS |
| CI e SonarCloud | Run 43265545752; Quality Gate OK; Reliability A; zero issues abertas no código novo | PASS para commit 3426e51 |
| RTM e links | ValidateRtm.java: 24 IDs e 174 links locais verificados | PASS estrutural |
| Clone reproduzível | Clone e clean verify executados; servidor e bootstrap interativo não executados | PARTIAL |
| Desempenho JMeter | JMX versionados; dados históricos sem JTL/HTML atual e sem JMeter instalado | PARTIAL |
| Interface visual | Teste de assets/erro em MockMvc; inspeção de navegador não concluída | PARTIAL |
| Revisão por pares | API do PR informa reviews vazios e reviewDecision em branco | PENDENTE |
| Release candidate | PR aberto; main não contém o commit; nenhuma tag criada | PENDENTE |

## Defeitos corrigidos

1. Sonar java:S2119 no bootstrap administrativo: SecureRandom agora é mantido
   como instância estática final e continua gerando bytes para a chave JWT.
2. Método de auditoria de conflito com chamada transacional interna: a
   serialização do Map agora acontece no método público anotado com
   REQUIRES_NEW, sem delegação self-invocation. Testes PostgreSQL de conflito
   e regressão passaram na suíte completa.
3. Documentação desatualizada para o estado Git/CI/Sonar: relatórios deste
   pacote foram reconciliados com o commit e os checks atuais.

## Riscos não aceitos pelo grupo

| Risco | Impacto | Mitigação/evidência faltante | Responsável |
|---|---|---|---|
| Sem nova execução JMeter reproduzível | desempenho e carga não podem ser avaliados neste commit | executar JMX em PostgreSQL descartável e guardar resultados sanitizados | A designar pelo grupo |
| Interface não inspecionada visualmente | regressões de responsividade/usabilidade podem passar pelos testes de API | revisar desktop e celular e registrar evidência | A designar pelo grupo |
| Sem review do PR e sem sign-off | processo de revisão e decisão acadêmica não comprovados | revisão por pares e votação/assinaturas reais | Integrantes a confirmar |
| Commit ainda não integrado à main | não há commit de main para a tag RC | após aprovação, merge pelo responsável e CI verde em main | Responsável pelo repositório |

Nenhum risco foi declarado aceito porque não há registro de decisão do grupo.

## Autoria e contribuição observável

O histórico de commits da branch contém:

| Identidade GitHub observada | Commits na branch do PR | Observação |
|---|---|---|
| guimaraesjamespotter07-del | f58a68e, 596ce05 | Identidade pública do Git; nome civil/contribuição declarada não confirmados |
| arthuralvesx | 3426e51 | Identidade pública do Git; contribuição registrada no commit de correção |

Esses dados não identificam a composição completa do grupo nem substituem a
declaração individual. Não foram inferidos nomes completos para o formulário.

## Assinatura do grupo

- Integrantes: pendente de confirmação dos nomes completos.
- Decisão/votação: não fornecida.
- Responsáveis pelos riscos: pendentes de designação.
- Assinaturas: pendentes.

## Próximos passos para a decisão final

1. Completar execução JMeter isolada e inspeção visual.
2. Obter review por pares do PR.
3. Confirmar nomes, contribuições, responsáveis e aprovação do grupo.
4. Integrar pelo processo normal, sem force push.
5. Validar CI no commit de main.
6. Solicitar autorização explícita do responsável para a tag RC e envio.

Não executar merge, criar/enviar tag ou preencher formulário com base neste
rascunho.
