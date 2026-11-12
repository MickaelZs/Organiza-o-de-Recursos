# ADR-0005 — Registro de não comparecimento

## Contexto

O domínio possui o estado `NAO_COMPARECEU`, mas o fluxo de registro precisa respeitar a máquina de estados e a autorização operacional. A especificação oficial não define uma janela temporal obrigatória para essa confirmação.

## Decisão

- O endpoint `POST /api/reservas/{id}/nao-compareceu` permite somente a transição `APROVADA → NAO_COMPARECEU`.
- Apenas `RESPONSAVEL` e `ADMIN` podem registrar a ocorrência; o solicitante não pode alterar arbitrariamente o estado da própria reserva nem de terceiros.
- Não é aplicada regra temporal inventada. A decisão fica a cargo do perfil operacional no momento da conferência.
- Chamadas repetidas ou reservas em qualquer outro estado retornam `409`, sem nova auditoria ou notificação.
- A operação grava usuário, reserva, estado anterior/novo, recurso, intervalo e horário na auditoria estruturada e notifica o solicitante.

## Consequências

`NAO_COMPARECEU` continua sendo estado não bloqueante para novos conflitos, conforme ADR-0002. O comportamento é explícito, transacional e verificável pelos testes PostgreSQL de `ReservaApiPostgresTest`.

