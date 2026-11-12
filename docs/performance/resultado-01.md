# Resultado JMeter 01 — 12/11/2026

## Repetição após correção — 12/11/2026

A pilha capturada durante 16 requisições mostrou transações aguardando uma conexão Hikari para auditoria `REQUIRES_NEW` enquanto mantinham outra conexão e locks; cinco requisições retornaram 500 após cerca de 30 segundos. A correção registra o conflito na mesma transação, antes de qualquer mudança de domínio; a transação confirma apenas a auditoria no conflito esperado. Exceções técnicas continuam provocando rollback. Na reprodução corrigida, 16 requisições retornaram 409 em até 1,01 s.

Com JMeter 5.6.3, A e E em paralelo com consulta: sala: 10 amostras, 1×201 e 9×409, média 416,6 ms, p90/p95/p99 607/644/644 ms e 14,53 amostras/s; manutenção: quatro 409, média 214,8 ms, p90/p95/p99 285/285/285 ms, 12,66 amostras/s; preparação: um 201; consulta: 100×200, média 26,3 ms, p90/p95/p99 47/60/96 ms, 3,76 amostras/s. Nenhum 500. Uma consulta SQL encontrou zero pares de reservas ativas de sala sobrepostas.

Na execução combinada dos demais grupos houve 100 consultas 200, mas quatro 404 de professor inexistente; a preparação estava incompleta. Após cadastrar professor sintético, o cenário C isolado retornou um 201 e três 409, sem erros. Os grupos combinados não substituem a verificação isolada de todos os resultados esperados; permanecem pendentes novas execuções isoladas e integridade global por todos os tipos de recurso. JTLs locais em `/tmp/qa-jmeter` são temporários. A execução anterior está preservada abaixo.


Base `33bf864` com correção local dos planos JMX, Java 21.0.13, JMeter 5.6.3 (SHA-512 do arquivo oficial conferido), Spring Boot local e PostgreSQL 16 descartável `qa-postgres-final`. Dados e contas sintéticos. As duas execuções abaixo ocorreram simultaneamente; suas latências não são uma medição isolada de capacidade. JTLs locais em `/tmp/qa-jmeter/` são temporários e não constituem artifact durável. Não foram gravados tokens nos resultados.

| Amostra | N | HTTP | Média ms | p90 / p95 / p99 ms | Mín / máx ms | Vazão aprox. amostras/s |
|---|---:|---|---:|---|---|---:|
| Reserva concorrente (10 threads) | 10 | 1×201, 8×409, 1×500 | 27657,90 | 31064 / 31135 / 31135 | 169 / 31135 | 0,32 |
| Reserva em manutenção (4 threads) | 4 | 1×409, 3×500 | 30193,25 | 30457 / 30457 / 30457 | 30077 / 30457 | 0,13 |
| Criação de manutenção | 1 | 1×201 | 986 | 986 / 986 / 986 | 986 / 986 | 1,01 |
| Consulta (10 threads, 10 iterações, ramp-up 30 s) | 100 | 100×200 | 1727,59 | 174 / 15220 / 27218 | 25 / 29730 | 3,29 |

Percentis pelo método nearest-rank; vazão por intervalo entre o primeiro início e a última conclusão de cada amostra. 409 são conflitos de negócio esperados; quatro 500 são erros técnicos e bloqueiam aprovação. A consulta SQL contou uma reserva para a sala e intervalo do cenário concorrente; isso não prova integridade global ou ausência de conflitos em todos os recursos. Os cenários B, C e D, a integridade global e nova execução isolada permanecem pendentes. Não há meta de latência documentada.

A primeira tentativa produziu somente 401 porque os planos usavam `__env`, não resolvida no JMeter padrão; acesso com o mesmo token retornou 200. Os planos foram alterados para ler as variáveis com `__groovy(System.getenv(...))`. A segunda execução acima comprova autenticação, mas expõe erro 500 sob concorrência; sua causa ainda não foi confirmada. Não declarar carga aprovada.

Em diagnóstico posterior, dez requisições concorrentes isoladas para a sala já reservada retornaram dez HTTP 409 e nenhum 500. A preparação de manutenção retornou 409 porque a manutenção sintética anterior já existia. Este diagnóstico não reproduziu a carga combinada e não resolve a falha anterior.
