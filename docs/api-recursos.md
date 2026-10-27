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

Os parametros podem ser combinados nas buscas de Sala e Material; todos os criterios informados devem corresponder. Uma busca valida sem resultado retorna `200 OK` com uma colecao vazia. `404 Not Found` permanece reservado para consulta individual por identificador inexistente.

Disponibilidade temporal nao e um filtro desta API. Ela depende do intervalo solicitado e de reservas/conflitos futuros (RF-04/RF-05), alem de manutencao no RF-08; `ativo` nao e usado como sinonimo de disponibilidade.
