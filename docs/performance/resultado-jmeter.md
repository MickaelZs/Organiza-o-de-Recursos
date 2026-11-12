# Resultado JMeter

> Registro histórico de execuções locais datadas de 08/11/2026 e 10/11/2026.
> Nesta auditoria os JTL/HTML referidos não foram encontrados no workspace ou
> no artifact CI, e JMeter não estava instalado. As métricas abaixo não foram
> recalculadas nem atribuídas ao commit 3426e51. O estado atual está em
> [resultado-01.md](resultado-01.md).

Este documento separa a execução de consulta, que tem métricas agregadas
versionadas, das execuções exploratórias dos cenários A–F, cujos JTLs foram
gerados localmente em `target/` e não acompanham o Git.

## Consulta de disponibilidade — execução local observada

- Data: 08/11/2026.
- Ambiente: local, `http://localhost:8080`, PostgreSQL 18.4.
- Java: 21.
- JMeter: 5.6.3.
- Plano: `docs/performance/organizacao-recursos-consulta.jmx`.
- Carga: 10 usuários virtuais, 10 iterações por usuário, ramp-up de 30 s.
- Endpoint: `GET /api/salas?tipo=Aula`.
- Assertion: HTTP 200.

| Métrica | Resultado |
|---|---:|
| Samples | 100 |
| HTTP 200 | 100 |
| Errors | 0 |
| Error rate | 0,00% |
| Average | 7,17 ms |
| Median | 6,00 ms |
| p90 | 8,00 ms |
| p95 | 9,95 ms |
| p99 | 61,60 ms |
| Min | 5,00 ms |
| Max | 62,00 ms |
| Throughput | 3,7288 samples/s |

Os percentis e a vazão foram extraídos do `statistics.json` do relatório HTML
gerado pelo JMeter. Não foi aplicada meta artificial de latência; o critério
registrado foi HTTP 200 em todas as amostras e error rate de 0%.

**Status:** PASS local observado para este cenário específico; JTL e HTML não são evidência portável do clone.

## Cenários A–F — execução local de verificação de regras

Em 10/11/2026, o plano `organizacao-recursos-carga.jmx` foi executado em
cenários separados, com assertions que aceitam 201 ou 409 nos conflitos e
exigem 409 para tentativa em manutenção. O resumo abaixo foi calculado dos
JTLs locais, sem copiar tokens ou respostas privadas para a documentação.

| Execução | Samples | Códigos observados | Falhas de assertion | Resultado |
|---|---:|---|---:|---|
| A-2 — mesmo recurso | 11 | 201=2, 409=9 | 0 | PASS local |
| B — recursos independentes | 5 | 201=3, 409=2 | 0 | PASS local |
| C — sobreposição de sala | 5 | 201=2, 409=3 | 0 | PASS local |
| C-professor — sobreposição de professor | 5 | 201=2, 409=3 | 0 | PASS local |
| D — intervalos adjacentes | 5 | 201=3, 409=2 | 0 | PASS local |
| E-2 — recurso em manutenção | 5 | 201=1, 409=4 | 0 | PASS local |
| F — carga de leitura | 101 | 200=100, 201=1 (preparação) | 0 | PASS local |

As amostras incluem a requisição de preparação de manutenção quando aplicável;
por isso o total não é sempre igual ao número de threads do grupo. O código
201 é esperado para a criação de preparação e 409 é esperado nos conflitos
assertados.

## Execuções não aprovadas e limitações

- `carga.jtl` e `carga-2.jtl`: 401 em todas as amostras por token ausente; não são resultados funcionais.
- `carga-3.jtl`: execução combinada registrou 500/timeouts em parte das amostras; não foi usada para declarar PASS.
- As primeiras execuções A e E também tiveram falhas de assertion; A-2 e E-2 foram as repetições finais usadas no resumo.
- JTLs, HTML, logs e os arquivos locais de token permanecem em `target/`, ignorados pelo Git. Não devem ser compartilhados sem sanitização.
- As execuções individuais verificam regras e não medem capacidade de produção. Não há meta de latência oficial registrada.
- JMeter não substitui os testes PostgreSQL de concorrência nem comprova uma única reserva aceita em qualquer perfil de carga.

## Reprodução

Consulte o [plano JMeter](plano-jmeter.md) para pré-condições, variáveis de
ambiente e comandos. Tokens devem ser fornecidos somente por ambiente local;
nunca por argumentos documentados, arquivos versionados ou logs.
