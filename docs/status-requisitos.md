# Status dos requisitos

Status consolidado após a integração final de 03/11/2026. A suíte `clean verify` terminou em BUILD SUCCESS: 147 testes, 0 falhas, 0 erros e 0 ignorados. JaCoCo registrou 96,37% de linhas (451/468) e 83,87% de branches (156/186), superando as metas de 80% e 70%. A matriz detalhada está em `docs/rtm.md`.

| Requisito | Implementado | Validado | Pendente / limite | Status |
|---|---|---|---|---|
| RF-01 — autenticação e autorização por perfil | Sim: BCrypt, JWT stateless e papéis do sistema. | Sim: testes de autenticação, JWT, 401/403 e autorização de integração; suíte final verde. | Sem pendências conhecidas no escopo. | IMPLEMENTADO E VALIDADO |
| RF-02 — cadastro e consulta de recursos | Sim: Sala, Professor e Material, com validação e autorização ADMIN para cadastro. | Sim: testes de API e repository em PostgreSQL/Testcontainers. | Sem pendências conhecidas no escopo. | IMPLEMENTADO E VALIDADO |
| RF-03 — busca e disponibilidade | Sim: filtros estruturais e disponibilidade temporal para os três tipos de recurso, inclusive filtros combinados. | Sim: 24 testes em `ResourceSearchApiPostgresTest`, além da suíte final. | Manutenção (RF-08) ainda não é considerada; `ativo` não significa disponibilidade. | IMPLEMENTADO E VALIDADO |
| RF-04 — reservas | Sim: criação, consulta, atualização e cancelamento lógico, com ownership do solicitante. | Sim: API e persistência validadas contra PostgreSQL/Testcontainers. | Auditoria e fluxos de retirada/devolução fora do escopo. | IMPLEMENTADO E VALIDADO |
| RF-05 — conflitos | Sim: sobreposição por recurso com intervalo semiaberto; estados bloqueantes conforme ADR-0002. | Sim: conflito, adjacência, recursos distintos e busca temporal validados em PostgreSQL. | Política de estados depende da decisão documentada em ADR-0002. | IMPLEMENTADO E VALIDADO |
| RF-06 — concorrência | Sim: lock pessimista da linha do recurso na transação, conforme ADR-0003. | Sim: cenários concorrentes executados contra PostgreSQL/Testcontainers. | Escritas que não sigam o fluxo transacional da aplicação não recebem essa proteção. | IMPLEMENTADO E VALIDADO |
| RF-07 — aprovação de recursos restritos | Sim: aprovação/rejeição por RESPONSAVEL de reserva restrita pendente, conforme ADR-0004. | Sim: perfis, estados, conflitos e transições inválidas validados em PostgreSQL/Testcontainers. | Uso, conclusão, manutenção, auditoria e notificações estão fora do fluxo. | IMPLEMENTADO E VALIDADO |
| RF-08 — RF-14 | Não. | Não declarados como validados. | Próximos requisitos do projeto. | NÃO IMPLEMENTADOS |

## Evidência final

- Testes focados (integração, segurança e reservas): 122 testes, sem falhas ou erros, BUILD SUCCESS.
- `clean verify`: 147 testes; 0 falhas; 0 erros; 0 ignorados; BUILD SUCCESS.
- JaCoCo: linhas 96,37% (451/468); branches 83,87% (156/186).
- PostgreSQL real via Testcontainers; Flyway aplicou V1–V6 em banco limpo e alcançou schema v6.

As estratégias de conflito, concorrência e aprovação estão registradas, respectivamente, nas ADRs 0002, 0003 e 0004. RF-08 ou posteriores não são declarados implementados.
