# Relatório de testes

## Execução final comprovada

A execução final desta auditoria foi realizada com Java 21 e Docker Desktop disponível para o Testcontainers:

```text
clean verify
Java 21
192 testes
0 failures
0 errors
0 skipped
BUILD SUCCESS
JaCoCo: 93,03% linhas (4.616/4.962)
JaCoCo: 76,44% branches (266/348)
```

Os contadores foram lidos dos relatórios Surefire e JaCoCo gerados pelo build. Nenhum resultado de CI, SonarCloud ou JMeter é inferido a partir do código.

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

Não há execução local comprovada de GitHub Actions ou SonarCloud nesta auditoria. O JMeter possui plano e arquivo `.jmx`, mas o resultado de carga permanece pendente até existir ambiente com aplicação, PostgreSQL, credencial de teste e JMeter executável.
