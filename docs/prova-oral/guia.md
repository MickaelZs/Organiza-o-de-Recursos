# Guia de prova oral

## Arquitetura

Explique o monólito Spring Boot em camadas: interface estática/cliente REST, controllers, services, repositories e PostgreSQL com Flyway. A decisão evita complexidade distribuída e mantém as regras testáveis.

## Regras principais

- intervalo válido sempre tem `termino > inicio`;
- conflito usa intervalos semiabertos `[inicio, termino)` e considera sala, professor e material;
- `SOLICITADA`, `APROVADA`, `EM_USO` e `CONCLUIDA` bloqueiam o horário;
- recursos restritos começam como `SOLICITADA` e somente `RESPONSAVEL` aprova/rejeita;
- manutenção ativa também bloqueia disponibilidade;
- retirada leva a `EM_USO` e devolução a `CONCLUIDA`;
- reservas iniciadas/concluídas não podem ser canceladas;
- mudanças de reserva geram auditoria e as operações relevantes geram notificação interna.

## Segurança

JWT stateless, BCrypt para senhas, autorização por perfil e ownership. 401 representa autenticação ausente/inválida; 403 representa falta de autorização. Respostas não expõem senha, hash, segredo ou stack trace.

## Concorrência

O serviço bloqueia pessimisticamente a linha do recurso antes de consultar conflitos. `ReservaConcorrenciaPostgresTest` prova que duas reservas simultâneas conflitantes produzem exatamente um sucesso; o teste de manutenção cobre a disputa reserva × manutenção.

## Qualidade

- JUnit 5, unitários, parametrizados e integração;
- API caixa-preta e E2E semântico via MockMvc + PostgreSQL;
- Testcontainers usa PostgreSQL real;
- RF08 preserva TDD RED/GREEN em `7948ce7` (04/11/2026, testes RED de manutenção) seguido de `6a758d2` (04/11/2026, implementação de manutenção); esses commits são históricos e não o HEAD atual;
- JaCoCo é gerado no `verify`;
- GitHub Actions executa `clean verify` em push/PR;
- SonarCloud está configurado condicionalmente, mas depende de secret e variáveis do repositório;
- JMeter tem plano versionado; consulta e cenários A–F possuem resultados locais agregados, mas JTL/HTML não são artefatos portáveis e a execução ampla preliminar teve falhas registradas;
- WireMock não é aplicável porque não há integração HTTP externa.
