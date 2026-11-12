# Busca de recursos

As rotas de consulta exigem um JWT valido. Sem parametros, mantem o comportamento de listagem do RF-02.

| Endpoint | Parametro opcional | Significado |
|---|---|---|
| `GET /api/salas` | `tipo` | Trecho do tipo, sem diferenca entre maiusculas e minusculas. |
| `GET /api/salas` | `capacidadeMinima` | Capacidade minima inclusiva; deve ser maior ou igual a zero. |
| `GET /api/salas` | `localizacao` | Trecho da localizacao, sem diferenca entre maiusculas e minusculas. |
| `GET /api/professores` | `competencia` | Trecho da competencia, sem diferenca entre maiusculas e minusculas. |
| `GET /api/materiais` | `tipo` | Trecho do tipo, sem diferenca entre maiusculas e minusculas. |
| `GET /api/materiais` | `localizacao` | Trecho da localizacao, sem diferenca entre maiusculas e minusculas. |
| `GET /api/salas`, `/api/professores`, `/api/materiais` | `inicio` e `termino` | Intervalo ISO-8601 com offset; devem ser informados juntos e `termino` deve ser posterior a `inicio`. |

Os parametros podem ser combinados nas buscas de Sala e Material; todos os criterios informados devem corresponder. Uma busca valida sem resultado retorna `200 OK` com uma colecao vazia. `404 Not Found` permanece reservado para consulta individual por identificador inexistente.

Quando `inicio` e `termino` sao informados juntos, cada busca exclui recursos com reserva ou manutenção ativa sobreposta ao intervalo. Sem os dois parametros, a busca mantém a listagem estrutural. A consulta considera as reservas e os estados bloqueantes definidos para RF-05 e as manutenções ativas de RF-08. `ativo` não é usado como sinonimo de disponibilidade.

Criação, consulta individual e os demais fluxos operacionais estão no [catálogo completo da API](api-fluxos.md).
