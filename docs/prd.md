# PRD — Organização de Recursos

Sistema acadêmico para gerenciar recursos físicos e humanos, reservas e os fluxos operacionais associados.

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
| RF-11 | Notificações persistidas e consultáveis | IMPLEMENTADO |
| RF-12 | Relatórios de utilização, carga horária e conflitos evitados | IMPLEMENTADO |
| RF-13 | Interface REST e mensagens de erro compreensíveis | IMPLEMENTADO |
| RF-14 | Documentação da API e dos fluxos públicos | IMPLEMENTADO |

## Escopo de RF12

RF12 é atendido por três consultas REST com período obrigatório: utilização por tipo de recurso, carga horária por solicitante e conflitos evitados. Os dados são agregados a partir de reservas/auditorias persistidas em PostgreSQL.

## Interpretação de RF13

O sistema entregue é backend/API. Portanto, “interface” foi tratado como contrato REST, códigos HTTP e tratamento global de erros. Não foi criado frontend visual porque não há requisito documentado para React, HTML, Thymeleaf ou dashboard.

## Fonte de verdade

O contrato utilizado nesta entrega é o PRD deste repositório e seus testes de integração. Não foi encontrado no workspace um enunciado do professor específico para RF12–RF14 aplicável a este projeto.
