# Status dos requisitos

Este quadro é um resumo da [RTM detalhada](rtm.md). `PASS` representa validação local de comportamento; não significa aprovação remota, release ou clone limpo.

| Requisito | Implementação | Evidência principal | Status da auditoria |
|---|---|---|---|
| RF-01 Autenticação e autorização | JWT, BCrypt, perfis e ownership | `SecurityAuthorizationIntegrationTest` | PASS local |
| RF-02 Salas, professores e materiais | APIs, persistência e autorização por perfil | testes de autorização e repositories PostgreSQL | PASS local |
| RF-03 Busca e disponibilidade | filtros estruturais e temporais | `ResourceSearchApiPostgresTest` | PASS local |
| RF-04 Criação, alteração e cancelamento | transições e ownership | `ReservaApiPostgresTest` | PASS local |
| RF-05 Sobreposição temporal | sala, professor, material e manutenção | testes PostgreSQL de reserva e busca | PASS local |
| RF-06 Concorrência | lock pessimista por recurso | `ReservaConcorrenciaPostgresTest` | PASS local |
| RF-07 Aprovação restrita | decisão do perfil RESPONSAVEL | `ReservaApiPostgresTest` e testes 403 | PASS local |
| RF-08 Manutenção | bloqueio e disputa Reserva × Manutenção | `ManutencaoApiRedTest` e concorrência PostgreSQL | PASS local |
| RF-09 Retirada e devolução | transições e timestamps | `ReservaRetiradaDevolucaoPostgresTest` | PASS local |
| RF-10 Auditoria | snapshots, estado, recurso, usuário e intervalo | `AuditoriaPostgresTest` | PASS local |
| RF-11 Notificações | persistência e isolamento por destinatário | `NotificacaoPostgresTest` | PASS local |
| RF-12 Relatórios | utilização, carga horária e conflitos evitados | `RelatorioPostgresTest` | PASS local |
| RF-13 Interface e erros | interface estática e contrato JSON | `InterfaceErrosIntegrationTest` | PARCIAL: falta inspeção visual |
| RF-14 Documentação pública | README, API e documentos de engenharia | revisão documental | PARCIAL: clone limpo e revisão por pares pendentes |
| RNF-01 Cobertura JaCoCo 80/70 | metas preservadas no POM | relatório e `jacoco-check` local | PASS local |
| RNF-02 JUnit/Testcontainers | PostgreSQL real em testes de integração | 214 testes, nenhum ignorado | PASS local; WireMock não aplicável |
| RNF-03 CI e SonarCloud | workflow condicional existente | execução remota mais recente anterior à revisão | PARCIAL: execução para esta árvore pendente |
| RNF-04 Desempenho, segurança e robustez | JMeter, JWT, validação e erros | relatório de carga e registro de defeitos | PARCIAL: investigação de HTTP 500 e token em log |

## Evidência da execução local

Em 12/11/2026, `.\mvnw.cmd -B verify` terminou com `BUILD SUCCESS`: 214 testes passaram, com 0 falhas, 0 erros e 0 ignorados. O JaCoCo mediu LINE 89,22% (1.092/1.224) e BRANCH 72,07% (320/444); o goal configurado aprovou. Uma tentativa anterior sem acesso ao named pipe do Docker permanece documentada como diagnóstico, sem substituir a execução final. A distinção está documentada no [relatório de testes](testes/relatorio-testes.md).

## Critérios ainda não comprovados

- CI/SonarCloud sobre a árvore de trabalho desta auditoria;
- clone limpo usando somente as instruções do README;
- inspeção visual manual da interface;
- resultado de estresse sem respostas HTTP 500;
- pacote de evidências portável, sem depender de saídas locais ignoradas;
- nomes completos e declaração de contribuição de todos os integrantes.
