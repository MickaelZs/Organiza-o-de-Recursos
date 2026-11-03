# Matriz de rastreabilidade de requisitos

Evidência de integração final registrada em 03/11/2026: `.\mvnw.cmd -B '-Dmaven.repo.local=C:\Users\arthu\.m2\repository' clean verify` terminou em BUILD SUCCESS, com 147 testes, 0 falhas, 0 erros e 0 ignorados. Cobertura JaCoCo: 451/468 linhas (96,37%) e 156/186 branches (83,87%). Os testes de persistência executaram contra PostgreSQL via Testcontainers; Flyway aplicou V1–V6 em banco limpo, schema final v6. Ver detalhes em `README.md` e `docs/status-requisitos.md`.

| Requisito | Implementação | Validação/evidência | Pendente / limite | Status |
|---|---|---|---|---|
| RF-01 | Usuários com BCrypt, login JWT stateless, filtro JWT e autorização por perfil. | `AuthControllerTest`, `AuthServiceTest`, `JwtServiceTest`, `JwtAuthenticationFilterTest`, `SecurityAuthorizationIntegrationTest` e testes de autorização dos recursos/reservas; 401 e 403 exercitados. Suite final: 147 testes, BUILD SUCCESS. | Não há pendência conhecida no escopo RF-01. | IMPLEMENTADO E VALIDADO |
| RF-02 | Cadastro e consulta de Sala, Professor e Material, com validação, persistência PostgreSQL e cadastro restrito a ADMIN. | `SalaAuthorizationIntegrationTest`, `ProfessorAuthorizationIntegrationTest`, `MaterialAuthorizationIntegrationTest` e respectivos testes `*RepositoryPostgresTest`; migrations V1–V5 e schema v6 inicializados no teste. | Operações adicionais fora do contrato dos recursos permanecem pendentes. | IMPLEMENTADO E VALIDADO |
| RF-03 | Busca estrutural de Sala, Professor e Material; disponibilidade temporal ISO-8601 com offset, combinável com filtros estruturais e baseada nas reservas bloqueantes. | `ResourceSearchApiPostgresTest` (24 testes), PostgreSQL/Testcontainers; inclui intervalo válido/inválido, combinação de filtros, ocupação e recursos sem reservas. | Disponibilidade não considera manutenção (RF-08). `ativo` não equivale a disponibilidade. | IMPLEMENTADO E VALIDADO |
| RF-04 | Criar, consultar, atualizar e cancelar reservas de Sala, Professor ou Material, com solicitante obtido do JWT e ownership nas operações do solicitante. | `ReservaApiPostgresTest` (33 testes) e `ReservaRepositoryPostgresTest` (14 testes), PostgreSQL/Testcontainers; criação válida, 404 de recurso inexistente, validação temporal, ownership e cancelamento lógico. | Fluxos de manutenção, retirada/devolução e auditoria não estão incluídos. | IMPLEMENTADO E VALIDADO |
| RF-05 | Detecção de sobreposição para o mesmo recurso. Intervalos semiabertos; adjacência permitida. Bloqueiam `SOLICITADA`, `APROVADA`, `EM_USO`, `CONCLUIDA`; liberam `REJEITADA`, `CANCELADA`, `NAO_COMPARECEU`. | `ReservaApiPostgresTest` e `ResourceSearchApiPostgresTest`, contra PostgreSQL; cobrem os três tipos de recurso, recursos distintos e disponibilidade. Decisão em ADR-0002. | A regra de estados é a decisão registrada em ADR-0002 e pode ser revisada se o contrato do professor mudar. | IMPLEMENTADO E VALIDADO |
| RF-06 | Lock pessimista de escrita na linha do recurso durante a transação, antes da consulta de conflito e gravação. | `ReservaConcorrenciaPostgresTest` (2 testes), PostgreSQL/Testcontainers: apenas uma criação concorrente para mesmo recurso/intervalo; recursos diferentes seguem independentes. Decisão e limites em ADR-0003. | Escritas diretas no banco ou caminhos que não obtenham o lock não são protegidos pela estratégia. | IMPLEMENTADO E VALIDADO |
| RF-07 | Sala/Material restritos iniciam em `SOLICITADA`; somente RESPONSAVEL aprova ou rejeita solicitação restrita pendente. Aprovação revalida conflitos sob lock. | `ReservaApiPostgresTest`, PostgreSQL/Testcontainers; criação pendente, perfis, aprovação, rejeição, conflito e transições inválidas. Decisão em ADR-0004. | Início de uso, conclusão, manutenção, auditoria e notificações não fazem parte deste fluxo. | IMPLEMENTADO E VALIDADO |
| RF-08 a RF-14 | Não implementados nesta entrega. | Não declarados como validados. | Permanecem fora do escopo concluído do Integrante 2. | NÃO IMPLEMENTADOS |

## Evidência da execução final

| Verificação | Resultado |
|---|---|
| Testes focados (integração, segurança e reservas, antes da suíte) | 122 testes; 0 falhas; 0 erros; BUILD SUCCESS |
| `clean verify` | 147 testes; 0 falhas; 0 erros; 0 ignorados; BUILD SUCCESS |
| JaCoCo — linhas | 451 cobertas / 468; 96,37% (meta >= 80%) |
| JaCoCo — branches | 156 cobertos / 186; 83,87% (meta >= 70%) |
| PostgreSQL/Testcontainers/Flyway | PostgreSQL 16.15 observado nos testes; Testcontainers; migrations V1–V6 aplicadas; schema v6 |

Os relatórios JaCoCo são artefatos locais em `target/site/jacoco` e não são versionados.
