# ADR-0004: aprovacao de reservas de recursos restritos

- Status: aceita
- Data: 2026-11-03

## Contexto

RF-07 exige aprovacao obrigatoria para recursos restritos. A especificacao atribui a aprovacao de solicitacoes especiais ao perfil RESPONSAVEL e define a transicao `SOLICITADA -> APROVADA`; estados alternativos incluem rejeicao. Sala e Material tem o atributo de restricao; Professor nao tem.

## Decisao

- Reservas de Sala ou Material restrito sao criadas como `SOLICITADA` e aguardam decisao.
- Recursos comuns, incluindo Professor, nao exigem aprovacao e sao confirmados como `APROVADA` na criacao. Esta regra interpreta a aprovacao obrigatoria como aplicavel somente a recursos restritos.
- Somente RESPONSAVEL pode aprovar ou rejeitar; SOLICITANTE e ADMIN recebem 403. O RESPONSAVEL so pode decidir reservas `SOLICITADA` que envolvam recurso restrito.
- A aprovacao muda `SOLICITADA` para `APROVADA`; rejeicao muda `SOLICITADA` para `REJEITADA`. Outras transicoes continuam fora deste fluxo e sao recusadas.
- A aprovacao revalida conflitos sob lock do recurso. `SOLICITADA` e `APROVADA` continuam bloqueando horario; `REJEITADA` libera disponibilidade, seguindo a ADR-0002.
- O solicitante continua consultando as proprias reservas; RESPONSAVEL e ADMIN consultam todas, conforme a matriz existente.

## Consequencias e limites

- Nenhuma migration e necessaria: os status existentes e os campos `restrita`/`restrito` sao suficientes.
- A reserva pendente ocupa o intervalo para impedir outra criacao conflitante antes da aprovacao.
- Este fluxo nao implementa inicio de uso, conclusao, manutencao, retirada/devolucao, auditoria ou notificacoes.
