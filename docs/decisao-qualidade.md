# Decisão de qualidade

## Identificação

- Projeto: Organização de Recursos.
- Repositório público: https://github.com/MickaelZs/Organiza-o-de-Recursos
- Release candidate: `v1.0.0-rc1`
- Commit integrado avaliado: `58d91835400f4539b7b98f3236e0db10e19896a0`
- PR integrado: [PR #12](https://github.com/MickaelZs/Organiza-o-de-Recursos/pull/12)
- CI pós-merge: [GitHub Actions 44409108743](https://github.com/MickaelZs/Organiza-o-de-Recursos/actions/runs/44409108743)
- Data: 18/11/2026.

## Decisão

**LIBERAR COM RESSALVAS como release candidate.**

O commit integrado em `main` passou pela CI remota e a verificação local registrou 215 testes aprovados. JaCoCo passou 80/70 no artifact de 89,84% LINE e 72,07% BRANCH. A equipe realizou o merge do PR #12.

Esta decisão não declara prontidão para produção. JMeter não possui um pacote final completo validado contra esta entrega e o aceite formal individual no GitHub não consta. Os riscos abaixo são limitados a um release candidate e exigem acompanhamento.

## Critérios

| Critério | Evidência | Resultado |
|---|---|---|
| Código integrado | Merge do PR #12 em `main`, commit `58d9183` | Aprovado |
| CI | Build verde na execução 44409108743 | Aprovado |
| Testes | 215 testes locais, 0 falhas, 0 erros, 0 ignorados | Aprovado no escopo |
| Cobertura | LINE 89,84%; BRANCH 72,07%; limites 80/70 | Aprovado |
| Requisitos e RTM | RTM versionada em [docs/rtm.md](rtm.md) | Presente |
| API e instalação | README e documentação da API presentes | Presente |
| Arquitetura | Diagramas de contexto, componentes e sequência presentes | Presente |
| SonarCloud | Quality Gate aprovado no PR #12 | Aprovado no PR |
| Carga | Planos e resultados históricos em [docs/performance/resultado-01.md](performance/resultado-01.md) | Parcial |
| Revisão por pares | PR integrado pela equipe; não há revisões individuais registradas no GitHub | Ressalva |
| Defeitos | [Registro de defeitos](defeitos/registro-defeitos.md) atualizado | Presente |

## Riscos aceitos

| Risco | Impacto | Justificativa | Responsável |
|---|---|---|---|
| Cobertura de carga não final | Limita conclusão de capacidade e latência em produção | JMeter pode ser repetido em ambiente descartável antes de promover RC2/produção | MickaelZs — responsável pelo repositório |
| JTL/HTML não consolidados no commit candidato | Dificulta auditoria independente de todos os cenários | Evidências históricas são preservadas; não foram ocultados 500 anteriores | Nabil Saba — responsável por qualidade/performance |
| Revisão individual não registrada | Menor rastreabilidade formal do processo de revisão | O merge foi realizado pela equipe e os checks remotos estão verdes | guimaraesjamespotter07-del — responsável pela revisão da entrega |
| Nomes civis não confirmados | Pode impedir preenchimento completo do formulário acadêmico | Identidades Git foram registradas; nomes civis devem ser declarados pelos integrantes | arthuralvesx — responsável pela declaração de autoria |

Esses riscos são aceitos somente para o release candidate. Não autorizam prontidão para produção.

## Integrantes e contribuição

Os nomes civis completos não foram fornecidos separadamente. A tabela usa as identidades Git observáveis no histórico:

| Identidade observada | Contribuição registrada |
|---|---|
| Mickael Vinicius / MickaelZs | Integração, CI, reservas, documentação e consolidação da entrega |
| guimaraesjamespotter07-del | Bootstrap administrativo e documentação/qualidade |
| arthuralvesx | Correções SonarCloud, interface, auditoria e ajustes finais |
| Nabil Saba | Auditoria de contenção, JMeter, evidências e documentação |

Cada integrante deve confirmar seu nome civil e sua contribuição no formulário. Não foram inferidos nomes completos não presentes no histórico.

## Limitações

Não há evidência desta rodada para afirmar bootstrap/login em clone final, inspeção física em dispositivos ou capacidade de produção. Os testes automatizados e o CI não substituem essas validações.

## Próxima revisão

Promover a versão candidata somente depois de:

1. Executar JMeter atualizado e sanitizado em banco descartável.
2. Registrar o resultado contra o commit da tag.
3. Confirmar nomes civis e declarações individuais.
4. Registrar revisão formal por pares.
5. Criar `v1.0.0-rc2` se houver correção, sem mover `v1.0.0-rc1`.

## Texto para o formulário

Repositório público: https://github.com/MickaelZs/Organiza-o-de-Recursos

Release candidate: `v1.0.0-rc1`

Commit avaliado: `58d91835400f4539b7b98f3236e0db10e19896a0`

Decisão: liberar com ressalvas para avaliação do release candidate. CI verde, 215 testes aprovados localmente e JaCoCo 89,84%/72,07%. JMeter final e nomes civis completos permanecem como ressalvas.
