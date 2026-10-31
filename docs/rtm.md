# Requirements Traceability Matrix

| Requisito | Risco | Implementacao | Teste | Evidencia | Status |
|---|---|---|---|---|---|
| RF-01 | Autenticacao indevida, token invalido e acesso nao autenticado | Usuario/Perfil, BCrypt, login JWT stateless, filtro JWT, 401 para ausencia/token invalido/expirado e matriz de autorizacao | `AuthServiceTest`, `AuthControllerTest`, `PasswordEncoderTest`, `JwtServiceTest`, `JwtAuthenticationFilterTest`, `SecurityAuthorizationIntegrationTest`, `UsuarioRepositoryPostgresTest` | Suite final `clean verify`: 93 testes, 0 failures, 0 errors, 0 skipped, BUILD SUCCESS; autenticacao/autorizacao e persistencia de Usuario validadas | IMPLEMENTADO E VALIDADO |
| RF-02 | Cadastro indevido, dados invalidos e estoque estruturalmente impossivel | Sala, Professor e Material, migrations Flyway, DTOs, repositories, APIs e autorizacao ADMIN para cadastro | `SalaAuthorizationIntegrationTest`, `SalaRepositoryPostgresTest`, `ProfessorAuthorizationIntegrationTest`, `ProfessorRepositoryPostgresTest`, `MaterialAuthorizationIntegrationTest`, `MaterialRepositoryPostgresTest` | Suite final `clean verify`: 93 testes, 0 failures, 0 errors, 0 skipped, BUILD SUCCESS; Sala, Professor, Material e migrations V1-V5 validados em PostgreSQL real | IMPLEMENTADO E VALIDADO |
| RF-03 | Busca estrutural incompleta ou disponibilidade temporal simulada | Filtros opcionais: Sala por tipo, capacidade minima e localizacao; Professor por competencia; Material por tipo e localizacao. Comparacoes textuais sao parciais e case-insensitive; capacidade minima usa `>=`. | `ResourceSearchApiPostgresTest` | 19 testes do ResourceSearch executados dentro da suite final com 0 failures/erros; filtros estruturais e HTTP 400 para capacidade negativa validados. Disponibilidade temporal depende de reservas, conflitos e manutencao futuros | PARCIALMENTE IMPLEMENTADO |
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
