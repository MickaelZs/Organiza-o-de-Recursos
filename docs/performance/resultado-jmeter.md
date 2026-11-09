# Resultado JMeter

## Execução real

- Data: 08/11/2026
- Ambiente: local, `http://localhost:8080`
- Java: 21
- PostgreSQL: 18.4
- JMeter: 5.6.3
- Plano: `docs/performance/organizacao-recursos-consulta.jmx`
- Usuários virtuais: 10
- Iterações por usuário: 10
- Ramp-up: 30 s
- Endpoint: `GET /api/salas?tipo=Aula`
- Assertion: HTTP 200

## Métricas observadas

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

Os percentis e a vazão foram extraídos do `statistics.json` gerado pelo relatório HTML do JMeter. Não foi aplicada meta artificial de latência; o critério de aprovação foi HTTP 200 em todas as amostras e error rate de 0%.

## Conclusão

**PASS.** A execução não gráfica terminou com 100 amostras, 100 respostas HTTP 200 e 0 erros. Os artefatos locais foram gerados em `target/jmeter/`, diretório protegido pelo `.gitignore`; nenhum JWT, senha ou hash foi documentado.
