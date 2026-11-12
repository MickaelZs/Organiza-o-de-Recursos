# Plano de teste de carga — JMeter

## Objetivo e planos

Os planos versionados exercitam duas perspectivas:

- `organizacao-recursos-consulta.jmx`: `GET /api/salas?tipo=Aula`, 10 usuários, 10 iterações e ramp-up de 30 s por padrão; assertion HTTP 200.
- `organizacao-recursos-carga.jmx`: cenários A–F de reserva concorrente, recursos independentes, conflito de sala/professor, intervalos adjacentes, manutenção e leitura concorrente. Os grupos são parametrizáveis separadamente para permitir repetir um cenário sem disparar os demais.

## Pré-condições

1. Java 21, PostgreSQL local com migrations aplicadas e aplicação executando.
2. Dados de teste válidos: salas indicadas por `roomId` e `otherRoomId`, professor indicado por `professorId` e períodos livres compatíveis com o cenário.
3. JWTs de teste válidos: `SOLICITANTE` e `RESPONSAVEL`.
4. JMeter 5.6.x instalado. Os arquivos `.jmx` declaram a versão 5.6.3.

Os planos de carga criam reservas/manutenções persistidas. Use apenas uma base local descartável de teste, nunca produção ou dados pessoais reais. Não limpe nem reaproveite uma base com dados importantes para preparar o cenário.

## Tokens sem exposição em logs

Não passe JWT por argumento `-J`: o JMeter pode registrar propriedades de linha de comando no `.jlog`. Defina-os no ambiente do processo. O plano lê `JMETER_AUTH_TOKEN` e, no plano A–F, `JMETER_RESPONSAVEL_TOKEN`.

PowerShell:

```powershell
$env:JMETER_AUTH_TOKEN = 'JWT_DE_TESTE_SOLICITANTE'
$env:JMETER_RESPONSAVEL_TOKEN = 'JWT_DE_TESTE_RESPONSAVEL'
```

Linux/macOS:

```bash
export JMETER_AUTH_TOKEN='JWT_DE_TESTE_SOLICITANTE'
export JMETER_RESPONSAVEL_TOKEN='JWT_DE_TESTE_RESPONSAVEL'
```

Use valores locais temporários e não salve comandos com valores reais em arquivos, histórico compartilhado, logs ou evidências.

## Execução do cenário de consulta

PowerShell:

```powershell
jmeter -n -t docs/performance/organizacao-recursos-consulta.jmx `
  -JbaseUrl=http://localhost:8080 `
  -Jthreads=10 -JrampUp=30 -Jloops=10 `
  -l target/jmeter/consulta-atual.jtl -e -o target/jmeter/consulta-atual
```

Linux/macOS:

```bash
jmeter -n -t docs/performance/organizacao-recursos-consulta.jmx \
  -JbaseUrl=http://localhost:8080 \
  -Jthreads=10 -JrampUp=30 -Jloops=10 \
  -l target/jmeter/consulta-atual.jtl -e -o target/jmeter/consulta-atual
```

## Execução dos cenários A–F

O grupo de preparação `E - preparar manutenção` é um `setUp Thread Group`; deixe-o habilitado. Ele cria a manutenção sintética para os cenários que a usam. Execute cada grupo isoladamente, definindo os demais grupos com zero threads. Exemplo do cenário A:

```powershell
jmeter -n -t docs/performance/organizacao-recursos-carga.jmx `
  -JbaseUrl=http://localhost:8080 -JroomId=1 -JotherRoomId=2 `
  -JaThreads=10 -JaRampUp=0 -JaLoops=1 `
  -JbThreads=0 -JcThreads=0 -JdThreads=0 -JeThreads=0 -JfThreads=0 `
  -l target/jmeter/carga-A.jtl -e -o target/jmeter/carga-A
```

Linux/macOS:

```bash
jmeter -n -t docs/performance/organizacao-recursos-carga.jmx \
  -JbaseUrl=http://localhost:8080 -JroomId=1 -JotherRoomId=2 \
  -JaThreads=10 -JaRampUp=0 -JaLoops=1 \
  -JbThreads=0 -JcThreads=0 -JdThreads=0 -JeThreads=0 -JfThreads=0 \
  -l target/jmeter/carga-A.jtl -e -o target/jmeter/carga-A
```

Repita com o grupo desejado (`bThreads` a `fThreads`) e os parâmetros específicos do cenário descritos no `.jmx`. Dê um nome de saída novo a cada execução: o JMeter não sobrescreve um diretório HTML existente.

## Métricas, interpretação e evidência

Registre amostras, códigos HTTP, assertions, throughput, média, p90, p95, p99, mínimo e máximo. Um HTTP 409 esperado por conflito precisa ser afirmado como resultado esperado no plano; o status não deve ser contabilizado como falha apenas por ser diferente de 2xx. Erros HTTP 500, timeouts e assertions falhadas permanecem falhas.

Não há meta artificial de latência definida pelo enunciado. As amostras pequenas dos cenários A–E servem para verificar regras; não sustentam uma conclusão de capacidade. A carga de leitura e os testes de estresse devem ser relatados separadamente, com ambiente, data, commit e limitações.

JTL e HTML são saídas locais em `target/`, ignoradas pelo Git. O relatório versionado deve conter apenas métricas agregadas e a localização/estado dos artefatos, sem JWT, cabeçalhos, senhas ou payloads privados. Consulte [resultado-jmeter.md](resultado-jmeter.md).
