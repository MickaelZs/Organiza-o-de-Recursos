# PRD — Organização de Recursos

Sistema acadêmico para gerenciar recursos físicos e humanos, reservas e os fluxos operacionais associados.

> **Leitura dos status:** `IMPLEMENTADO` descreve a presença da funcionalidade no código e sua validação local correspondente. A decisão de conformidade do requisito, incluindo pendências de evidência externa, fica na [RTM](rtm.md) e no [status dos requisitos](status-requisitos.md). Portanto, `IMPLEMENTADO` não equivale automaticamente a `PASS` global.

| Requisito | Descrição | Status atual |
|---|---|---|
| RF-01 | Autenticação e autorização por perfil | IMPLEMENTADO |
| RF-02 | Cadastro e consulta de salas, professores e materiais | IMPLEMENTADO |
| RF-03 | Pesquisa estrutural e disponibilidade temporal | IMPLEMENTADO |
| RF-04 | Criação, alteração, consulta e cancelamento de reservas | IMPLEMENTADO |
| RF-05 | Detecção de sobreposição de horários | IMPLEMENTADO |
| RF-06 | Proteção contra dupla reserva concorrente | IMPLEMENTADO |
| RF-07 | Aprovação de recursos restritos | IMPLEMENTADO |
| RF-08 | Bloqueio de recursos em manutenção | IMPLEMENTADO |
| RF-09 | Retirada e devolução | IMPLEMENTADO |
| RF-10 | Histórico auditável de mudanças | IMPLEMENTADO |
| RF-11 | Notificações simuladas internas, persistidas e consultáveis | IMPLEMENTADO |
| RF-12 | Relatórios de utilização, carga horária e conflitos evitados | IMPLEMENTADO |
| RF-13 | Interface responsiva e mensagens de erro compreensíveis | PARCIAL — contrato automatizado; inspeção visual pendente |
| RF-14 | Documentação da API e dos fluxos públicos | PARCIAL — documentação versionada; clone limpo/revisão por pares pendentes |

## Escopo de RF12

RF12 é atendido por três consultas REST com período obrigatório: utilização por recurso individual e por tipo, carga horária por solicitante e por professor alocado, e conflitos evitados. Utilização/carga usam somente `APROVADA`, `EM_USO` e `CONCLUIDA`, calculam a duração da interseção semiaberta `[inicio, termino)` e ignoram `REJEITADA`, `CANCELADA` e `NAO_COMPARECEU`. Conflitos são contados exclusivamente por auditorias `CONFLITO_EVITADO` persistidas.

## Interpretação de RF13

O sistema entregue possui uma interface web estática em HTML/CSS/JavaScript, além da API REST. Não há framework frontend separado; os painéis consomem os endpoints existentes e respeitam o perfil do JWT.

## Fonte de verdade

O contrato funcional é a [especificação oficial do projeto](https://afonsolelis.github.io/aulas_senac/pages/qualidade2/especificacao-projeto.html), complementada pelo PRD deste repositório e pelos testes de integração. O PRD não substitui a especificação oficial; divergências devem ser registradas na RTM.
