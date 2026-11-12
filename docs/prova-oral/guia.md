# Guia de prova oral

## Arquitetura

Explique o monólito Spring Boot em camadas: interface estática/cliente REST, controllers, services, repositories e PostgreSQL com Flyway. A decisão evita complexidade distribuída e mantém as regras testáveis.

## Regras principais

- intervalo válido sempre tem `termino > inicio`;
- conflito usa intervalos semiabertos `[inicio, termino)` e considera sala, professor e material;
- `SOLICITADA`, `APROVADA`, `EM_USO` e `CONCLUIDA` bloqueiam o horário;
- recursos restritos começam como `SOLICITADA` e somente `RESPONSAVEL` aprova/rejeita;
- manutenção ativa também bloqueia disponibilidade;
- retirada leva a `EM_USO` e devolução a `CONCLUIDA`;
- reservas iniciadas/concluídas não podem ser canceladas;
- mudanças de reserva geram auditoria e as operações relevantes geram notificação interna.

## Segurança

JWT stateless, BCrypt para senhas, autorização por perfil e ownership. 401 representa autenticação ausente/inválida; 403 representa falta de autorização. Respostas não expõem senha, hash, segredo ou stack trace.

## Concorrência

O serviço bloqueia pessimisticamente a linha do recurso antes de consultar conflitos. `ReservaConcorrenciaPostgresTest` prova que duas reservas simultâneas conflitantes produzem exatamente um sucesso; o teste de manutenção cobre a disputa reserva × manutenção.

## Qualidade

- JUnit 5, unitários, parametrizados e integração;
- API caixa-preta e E2E semântico via MockMvc + PostgreSQL;
- Testcontainers usa PostgreSQL real;
- RF08 preserva TDD RED/GREEN em `7948ce7` (04/11/2026, testes RED de manutenção) seguido de `6a758d2` (04/11/2026, implementação de manutenção); esses commits são históricos e não o HEAD atual;
- JaCoCo é gerado no `verify`; no commit 3426e51, clone limpo teve 90,01% LINE e 72,07% BRANCH, e o artifact CI teve 89,84%/72,07%;
- GitHub Actions executou `clean verify` para o commit 3426e51, com 214 testes, zero falhas/erros/ignorados;
- SonarCloud aprovou o PR #9 no mesmo commit: Quality Gate OK e Reliability A no código novo;
- JMeter tem planos versionados e relatórios históricos; a execução desta auditoria não foi repetida porque não havia JMeter nem JTL/HTML disponíveis. Não use os números antigos como resultado do commit atual;
- WireMock não é aplicável porque não há integração HTTP externa.

## Estado para a apresentação

O PR #9 continua aberto e sem review registrado. O build e o SonarCloud estão
verdes na branch, mas isso não equivale a CI em main nem à autorização de
release. A execução JMeter atual, inspeção visual, decisão assinada do grupo e
revisão por pares permanecem pendentes; consulte o
[relatório de qualidade](../relatorio-qualidade.md).
