# Requirements Traceability Matrix

| Requisito | Risco | Implementacao | Teste | Evidencia | Status |
|---|---|---|---|---|---|
| RF-01 | Autenticacao indevida, token invalido e acesso nao autenticado | Usuario/Perfil, BCrypt, login JWT stateless, filtro JWT, 401 para ausencia/token invalido/expirado e matriz de autorizacao | `AuthServiceTest`, `AuthControllerTest`, `PasswordEncoderTest`, `JwtServiceTest`, `JwtAuthenticationFilterTest`, `SecurityAuthorizationIntegrationTest`, `UsuarioRepositoryPostgresTest` | Testes sem Docker devem ser executados; o teste PostgreSQL requer Docker/Testcontainers | EM IMPLEMENTACAO |
| RF-02 | Cadastro indevido, dados invalidos e estoque estruturalmente impossivel | Sala, Professor e Material, migrations Flyway, DTOs, repositories, APIs e autorizacao ADMIN para cadastro | `SalaAuthorizationIntegrationTest`, `SalaRepositoryPostgresTest`, `ProfessorAuthorizationIntegrationTest`, `ProfessorRepositoryPostgresTest`, `MaterialAuthorizationIntegrationTest`, `MaterialRepositoryPostgresTest` | IMPLEMENTACAO COMPLETA / VALIDACAO PENDENTE: `testCompile` bloqueia execucao; persistencia requer Docker/Testcontainers | EM IMPLEMENTACAO |
| RF-03 | Busca estrutural incompleta ou disponibilidade temporal simulada | Filtros opcionais: Sala por tipo, capacidade minima e localizacao; Professor por competencia; Material por tipo e localizacao. Comparacoes textuais sao parciais e case-insensitive; capacidade minima usa `>=`. | `ResourceSearchApiPostgresTest` | TESTE CRIADO — EXECUCAO PENDENTE: o teste de contrato HTTP usa PostgreSQL/Testcontainers, mas `testCompile` ainda bloqueia a execucao. Disponibilidade temporal depende de RF-04/RF-05 e, posteriormente, RF-08. | PARCIALMENTE IMPLEMENTADO |
| RF-04 | Pendente | pendente | pendente | pendente | NAO IMPLEMENTADO |
| RF-05 | Pendente | pendente | pendente | pendente | NAO IMPLEMENTADO |
| RF-06 | Pendente | pendente | pendente | pendente | NAO IMPLEMENTADO |
| RF-07 | Pendente | pendente | pendente | pendente | NAO IMPLEMENTADO |
| RF-08 | Pendente | pendente | pendente | pendente | NAO IMPLEMENTADO |
| RF-09 | Pendente | pendente | pendente | pendente | NAO IMPLEMENTADO |
| RF-10 | Pendente | pendente | pendente | pendente | NAO IMPLEMENTADO |
| RF-11 | Pendente | pendente | pendente | pendente | NAO IMPLEMENTADO |
| RF-12 | Pendente | pendente | pendente | pendente | NAO IMPLEMENTADO |
| RF-13 | Pendente | pendente | pendente | pendente | NAO IMPLEMENTADO |
| RF-14 | Pendente | pendente | pendente | pendente | NAO IMPLEMENTADO |
