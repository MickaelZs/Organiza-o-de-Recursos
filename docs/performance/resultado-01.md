# Resultado JMeter 01 — 12/11/2026

## Cenários B e D isolados — 12/11/2026

Banco descartável, JMeter 5.6.3, Java 21, salas sintéticas 1 e 2; os demais grupos desativados. Em B, quatro solicitações para 10/02/2028 10h–11h (-03:00) produziram duas 201 e duas 409 esperadas, zero 500; média 77,8 ms, p90/p95/p99 94 ms, throughput 42,55 amostras/s. Preparação de manutenção em 11/02 retornou 201. Em D, duas threads solicitaram cada intervalo adjacente de 10/03/2028 10h–11h e 11h–12h: em cada intervalo houve uma 201 e uma 409, zero 500. Médias 50 e 39,5 ms; p90/p95/p99 56 e 46 ms; throughput 35,71 e 42,55 amostras/s, respectivamente. Preparação em 11/03 retornou 201. Percentis nearest-rank; throughput pelo primeiro início e última conclusão de cada grupo de amostras. Pequenas amostras verificam regras, não capacidade.

Dados sanitizados: [B](../evidencias/jmeter-2026-11-12/cenario-b-final.csv) e [D](../evidencias/jmeter-2026-11-12/cenario-d-final.csv). Consulta SQL posterior encontrou zero pares de reservas ativas sobrepostas de cada tipo de recurso.

## Complemento — material e evidências preservadas

Plano [material](organizacao-recursos-material.jmx), derivado do cenário A com `materialId` no corpo, JMeter 5.6.3 e banco PostgreSQL descartável: dez requisições simultâneas resultaram em um HTTP 201 e nove HTTP 409, zero 500; média 194,9 ms, p90 285 ms, p95/p99 304 ms, vazão 32,26 amostras/s. A preparação de manutenção retornou 201. Uma consulta após a carga encontrou zero pares de reservas ativas sobrepostas para cada tipo: sala, professor e material.

As amostras sanitizadas estão em [evidências JMeter](../evidencias/jmeter-2026-11-12/); preservam duração, código e instante relativo, sem URLs, tokens ou dados pessoais. Incluem a execução inválida com quatro 404 e a repetição de professor com dados corrigidos. O cenário material foi executado com `roomId=1`, `otherRoomId=2`, dez threads no grupo A e os demais grupos com zero threads; datas 2027-06-10 10h–11h e manutenção 2027-06-11 10h–11h, offset -03:00. Consulte [plano JMeter](plano-jmeter.md) para variáveis e execução. A repetição isolada de B e D está registrada acima.

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
