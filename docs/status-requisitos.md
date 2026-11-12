# Status dos requisitos

Este quadro resume a [RTM detalhada](rtm.md) e as evidências executadas no
commit 3426e51 (PR #9). PASS significa que o comportamento está ligado a testes
executados; os itens visuais, de carga e de aprovação humana são avaliados
separadamente.

| Requisito | Evidência principal | Status |
|---|---|---|
| RF-01 Autenticação e autorização | SecurityAuthorizationIntegrationTest, JwtServiceTest, AuthServiceTest | PASS |
| RF-02 Salas, professores e materiais | Testes de autorização e repositórios PostgreSQL | PASS |
| RF-03 Busca e disponibilidade | ResourceSearchApiPostgresTest | PASS |
| RF-04 Criar, alterar e cancelar reservas | ReservaApiPostgresTest e ReservaRepositoryPostgresTest | PASS |
| RF-05 Sobreposição temporal | Testes PostgreSQL de reserva e busca | PASS |
| RF-06 Concorrência | ReservaConcorrenciaPostgresTest e ManutencaoReservaConcorrenciaPostgresTest | PASS |
| RF-07 Aprovação de recursos restritos | ReservaApiPostgresTest e testes de autorização | PASS |
| RF-08 Manutenção | ManutencaoApiRedTest e testes de concorrência | PASS |
| RF-09 Retirada e devolução | ReservaRetiradaDevolucaoPostgresTest | PASS |
| RF-10 Auditoria | AuditoriaPostgresTest e RelatorioPostgresTest | PASS |
| RF-11 Notificações | NotificacaoPostgresTest | PASS |
| RF-12 Relatórios | RelatorioPostgresTest | PASS |
| RF-13 Interface responsiva e erros | InterfaceErrosIntegrationTest; inspeção visual ainda não comprovada | PARTIAL |
| RF-14 Documentação pública | README, guia de instalação, catálogo de API, OpenAPI e fluxos | PASS |
| RC-01 a RC-06 Regras críticas | RTM e testes PostgreSQL de estados, conflitos, manutenção e concorrência | PASS |
| RNF-01 Cobertura 80/70 | JaCoCo clean clone e artifact CI: 89,84% LINE / 72,07% BRANCH | PASS |
| RNF-02 JUnit/Testcontainers/E2E | 214 testes, 0 falhas, 0 erros, 0 ignorados; há API E2E com MockMvc/PostgreSQL, sem automação de navegador | PARTIAL |
| RNF-03 CI e SonarCloud | Build e SonarCloud aprovados para 3426e51 | PASS |
| RNF-04 Desempenho e robustez | Testes de segurança aprovados; evidência JMeter bruta não está disponível para reprodução | PARTIAL |

## Execução verificada

Em 12/11/2026, o commit 3426e51 foi clonado em diretório temporário e validado
com clean verify. O Testcontainers conectou ao Docker Desktop e executou os
testes contra PostgreSQL 16.15. Foram 214 testes, sem falhas, erros ou
ignorados. O artifact da execução remota 43265545752 contém os relatórios
Surefire e JaCoCo do mesmo commit.

O clone limpo reproduziu o build e a validação estrutural da RTM. A inicialização
manual do servidor, criação interativa do primeiro ADMIN e login visual não
foram executados nesse clone; são verificações distintas do build automatizado.

## Limitações da auditoria

- RF-13: falta inspeção visual em larguras de desktop e celular.
- RNF-02: os testes de API usam MockMvc e PostgreSQL; não há teste automatizado
  real de navegador.
- RNF-04: os arquivos JTL/HTML citados em relatórios antigos não existem no
  workspace atual e JMeter não está instalado; os números antigos são apenas
  históricos até uma nova execução isolada.
- Revisão por pares: o PR #9 não tem reviews registrados.
- Release: o commit aprovado está na branch do PR, ainda não em main; não há
  tag ou decisão assinada pelo grupo.

Veja [checklist da entrega](entrega/checklist.md), [índice de evidências](evidencias/README.md)
e [decisão de qualidade](decisao-qualidade.md).
