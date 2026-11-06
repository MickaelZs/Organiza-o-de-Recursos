# API de relatórios (RF12)

Todos os endpoints exigem perfil `RESPONSAVEL` ou `ADMIN` e os parâmetros obrigatórios `inicio` e `termino` em ISO-8601 com offset. O período é semiaberto `[inicio, termino)`, e `termino` deve ser posterior a `inicio`.

## Utilização

`GET /api/relatorios/utilizacao?inicio=...&termino=...`

Usa reservas persistidas com status `APROVADA`, `EM_USO` ou `CONCLUIDA`, calcula somente a interseção com o período e agrupa por `SALA`, `PROFESSOR` ou `MATERIAL`. A resposta contém totais e `porTipoRecurso`.

## Carga horária

`GET /api/relatorios/carga-horaria?inicio=...&termino=...`

Usa as mesmas reservas e agrupa as horas da interseção por solicitante. A resposta contém `totalHorasReservadas` e `porSolicitante`.

## Conflitos evitados

`GET /api/relatorios/conflitos-evitados?inicio=...&termino=...`

Conta registros persistidos em auditoria com ação `CONFLITO_EVITADO` no período. A ação é gerada quando uma nova reserva ou manutenção é rejeitada por sobreposição.

Período sem dados retorna HTTP 200 com coleções vazias e totais zero. Período invertido, ausente ou malformado retorna HTTP 400.
