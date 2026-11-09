# Plano de teste de carga — JMeter

## Objetivo

Medir o comportamento de uma consulta representativa de recursos (`GET /api/salas?tipo=Aula`) sob carga concorrente, observando throughput, tempo médio, percentis e taxa de erro.

## Cenário

- usuários virtuais: 10 por padrão, configurável por `-Jthreads`;
- ramp-up: 30 segundos por padrão, configurável por `-JrampUp`;
- iterações: 10 por usuário por padrão, configurável por `-Jloops`;
- endpoint: `GET /api/salas?tipo=Aula`;
- autenticação: JWT de teste informado por `-JauthToken`, nunca gravado no repositório;
- base URL: `http://localhost:8080` por padrão, configurável por `-JbaseUrl`;
- assertion: código HTTP 200.

## Pré-condições

1. Java 21, PostgreSQL configurado e aplicação executando.
2. Banco com migrations aplicadas e pelo menos uma sala do tipo `Aula`.
3. JWT de usuário autenticado válido para consulta.
4. JMeter 5.6.x instalado.

## Execução

Arquivo: `organizacao-recursos-consulta.jmx`.

```powershell
jmeter -n -t docs/performance/organizacao-recursos-consulta.jmx `
  -JbaseUrl=http://localhost:8080 `
  -JauthToken=JWT_DE_TESTE `
  -Jthreads=10 -JrampUp=30 -Jloops=10 `
  -l resultado.jtl -e -o target/jmeter-report
```

## Métricas e critérios

Registrar no relatório: número de amostras, throughput, média, p90, p95, p99, mínimo, máximo e percentual de erros. O critério mínimo funcional é 0% de respostas diferentes de 200; metas de latência/throughput devem ser definidas pelo professor ou pelo ambiente de entrega, não inventadas no repositório.
