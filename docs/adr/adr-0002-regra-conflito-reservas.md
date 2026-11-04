# ADR-0002: regra de conflito entre reservas

- Status: aceita
- Data: 2026-11-03

## Contexto

RF-05 proíbe sobreposição para o mesmo recurso (sala, professor ou material). A especificação enumera os estados de reserva, mas não detalha quais estados liberam o horário. A etapa 2C do guia exige uma decisão explícita e testes para estados não bloqueantes quando previstos pelo contrato.

## Decisão

Os intervalos são semiabertos, `[inicio, termino)`. Há conflito quando, para o mesmo recurso, `reservaExistente.inicio < novoTermino` e `reservaExistente.termino > novoInicio`. Assim, horários adjacentes são permitidos.

Bloqueiam horário: `SOLICITADA`, `APROVADA`, `EM_USO` e `CONCLUIDA`. A solicitação pendente ocupa o intervalo enquanto aguarda definição posterior; uma reserva concluída preserva o registro da alocação realizada.

Não bloqueiam horário: `REJEITADA`, `CANCELADA` e `NAO_COMPARECEU`, por serem estados alternativos que não representam alocação efetiva.

A consulta compara somente o mesmo identificador e tipo de recurso. Conflitos em recursos distintos não se afetam. Em alteração, a própria reserva é excluída da consulta.

## Consequências

- A aplicação consulta somente reservas candidatas no PostgreSQL e responde HTTP 409 quando encontra conflito.
- Esta decisão não implementa transições de estado, aprovação ou controle de concorrência; permanecem nas etapas correspondentes do guia.
- A regra precisa ser revista se o professor definir política diferente para estados terminais.
