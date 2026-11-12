# API de relatórios (RF12)

Todos os endpoints exigem perfil `RESPONSAVEL` ou `ADMIN` e os parâmetros obrigatórios `inicio` e `termino` em ISO-8601 com offset. O período é semiaberto `[inicio, termino)`, e `termino` deve ser posterior a `inicio`.

## Utilização

`GET /api/relatorios/utilizacao?inicio=...&termino=...`

Usa reservas persistidas com status `APROVADA`, `EM_USO` ou `CONCLUIDA`, calcula somente a interseção com o período e mantém o intervalo semiaberto `[inicio, termino)`. A resposta mantém `porTipoRecurso` e também expõe `porRecurso`, agrupado por tipo, ID, nome e categoria do recurso, com quantidade e horas utilizadas.

## Carga horária

`GET /api/relatorios/carga-horaria?inicio=...&termino=...`

Usa as mesmas reservas e agrupa as horas da interseção por solicitante (`totalHorasReservadas`/`porSolicitante`). Para reservas que possuem professor alocado, expõe adicionalmente `totalHorasAlocadas` e `porProfessor`, agrupados por professor, competência, quantidade de reservas e horas alocadas. Reservas de sala/material não são atribuídas artificialmente a um professor.

## Conflitos evitados

`GET /api/relatorios/conflitos-evitados?inicio=...&termino=...`

Conta registros persistidos em auditoria com ação `CONFLITO_EVITADO` no período. A ação é gerada quando uma nova reserva ou manutenção é rejeitada por sobreposição; a auditoria estruturada identifica a origem (`RESERVA` ou `MANUTENCAO`) e se o conflito foi de reserva ou manutenção. Não são contadas falhas técnicas genéricas nem respostas 409 de outras regras.

Período sem dados retorna HTTP 200 com coleções vazias e totais zero. Período invertido, ausente ou malformado retorna HTTP 400.
