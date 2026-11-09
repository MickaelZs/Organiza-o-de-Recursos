# Plano de testes

## Objetivo

Validar comportamento observável, regras de negócio, persistência PostgreSQL, autorização, concorrência e qualidade da API/interface sem substituir o banco real por H2.

## Estratégia e níveis

| Nível | Estratégia | Evidência no projeto |
|---|---|---|
| Unitário | Exercita componentes isolados de segurança, autenticação e regras simples | `AuthServiceTest`, `JwtServiceTest`, `JwtAuthenticationFilterTest`, `PasswordEncoderTest` |
| Parametrizado | Reutiliza cenários de perfis, recursos, intervalos e estados | `@ParameterizedTest` em testes de autorização, reservas, manutenção e busca |
| Integração | Inicializa o contexto Spring e valida Controller → Service → Repository | classes `*PostgresTest` e `*IntegrationTest` |
| API/caixa-preta | Usa MockMvc contra endpoints públicos, headers, payloads e status HTTP | `ReservaApiPostgresTest`, `ResourceSearchApiPostgresTest`, `InterfaceErrosIntegrationTest` |
| E2E semântico | Fluxos completos pela API, atravessando segurança, controller, serviço, JPA, Flyway e PostgreSQL | criação/aprovação/retirada/devolução em `ReservaApiPostgresTest` e `ReservaRetiradaDevolucaoPostgresTest` |
| Persistência | Restrições, enumerações, chaves estrangeiras, schema e consultas nativas | `*RepositoryPostgresTest`, migrations V1–V10 |
| Concorrência | Duas operações reais sincronizadas por `CountDownLatch` e executadas em `ExecutorService` | `ReservaConcorrenciaPostgresTest`, `ManutencaoReservaConcorrenciaPostgresTest` |
| Segurança | JWT válido, inválido e expirado; BCrypt; 401/403; ownership | `SecurityAuthorizationIntegrationTest`, testes de autorização e segurança |
| Performance | Plano JMeter versionável com endpoint de consulta e assertions | `docs/performance/plano-jmeter.md` e `organizacao-recursos-consulta.jmx` |

## Regras críticas

Os testes devem cobrir intervalo `termino > inicio`, conflito de sala/professor/material, disponibilidade, manutenção, aprovação restrita, retirada/devolução, cancelamento, `NAO_COMPARECEU`, auditoria e double booking concorrente. As fontes específicas estão na RTM e no relatório de testes.

## Critérios de aceite

- build Maven sem falhas, erros ou testes ignorados;
- cobertura JaCoCo mínima de 80% de linhas e 70% de branches;
- nenhuma violação de autorização conhecida;
- para duas solicitações conflitantes simultâneas do mesmo recurso, exatamente uma confirmação;
- erros 400, 401, 403, 404, 409 e 500 sem stack trace ou segredo na resposta;
- execução JMeter registrada separadamente, sem preencher métricas não executadas.
