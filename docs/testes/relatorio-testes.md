# Relatório de testes

## Execução final comprovada

A execução final desta auditoria foi realizada com Java 21 e Docker Desktop disponível para o Testcontainers:

```text
verify
Java 21
204 testes
0 failures
0 errors
0 skipped
BUILD SUCCESS
JaCoCo: 87,78% linhas (948/1.080)
JaCoCo: 70,09% branches (300/428)
JaCoCo check: PASS (mínimos de 80% de linhas e 70% de branches)
```

Os contadores foram lidos dos relatórios Surefire e JaCoCo gerados pelo build. O gate também foi comprovado com dois testes controlados: elevar cada mínimo para 101% fez o `verify` falhar; os valores oficiais de 80% e 70% permaneceram no `pom.xml`. Nenhum resultado remoto é atribuído à árvore local não publicada.

## Cobertura por tipo de evidência

- JUnit 5: classes de teste usam JUnit Jupiter.
- Unitários: autenticação, JWT, filtro e BCrypt.
- Parametrizados: perfis, tipos de recursos, estados e intervalos.
- Integração/API caixa-preta: MockMvc com rotas e respostas observáveis.
- E2E semântico: fluxo de reserva completo usando o contexto Spring e PostgreSQL.
- Testcontainers/PostgreSQL: testes `*PostgresTest` com `PostgreSQLContainer` `postgres:16-alpine`.
- Concorrência: `CountDownLatch` e `ExecutorService` nos testes dedicados.
- TDD: RF08 preserva os commits RED `7948ce7` e GREEN `6a758d2`.
- Autorização e validação: testes de 401/403, ownership, payload e intervalos.
- Erros seguros: `InterfaceErrosIntegrationTest` valida 400, 401, 403, 404, 302/200 do Springdoc e 500 sem stack trace.

## Limitações

O workflow remoto e o SonarCloud possuem evidência para o commit remoto `d0100a1`, mas essa execução antecede as alterações locais da Etapa 2 e não comprova esta árvore não publicada. A validação remota atual depende de push/autorização posterior. O JMeter possui plano e arquivo `.jmx`, mas o resultado de carga permanece pendente até existir ambiente com aplicação, PostgreSQL, credencial de teste e JMeter executável.
