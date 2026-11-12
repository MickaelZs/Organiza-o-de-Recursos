# Catálogo de API e fluxos implementados

Este documento complementa [API de recursos](api-recursos.md) e [API de relatórios](api-relatorios.md) com os endpoints e transições implementados nos controllers atuais. Todas as rotas de negócio exigem JWT, exceto health, login, interface estática e documentação OpenAPI.

## Convenções

- Base URL local: `http://localhost:8080`.
- Datas: ISO-8601 com offset, por exemplo `2026-11-04T09:00:00-03:00`.
- Respostas de erro: `timestamp`, `status`, `error`, `message` e `path`; detalhes em [api-erros.md](api-erros.md).
- `201 Created` é usado na criação e inclui o header `Location`; `204 No Content` é usado no cancelamento.
- `400` indica payload/parâmetro inválido, `401` falta de autenticação, `403` falta de permissão, `404` recurso inexistente e `409` conflito ou transição de estado inválida.

## Saúde, autenticação e documentação

| Método e caminho | Acesso | Comportamento |
|---|---|---|
| `GET /api/health` | público | Retorna o estado da aplicação. |
| `POST /api/auth/login` | público | Recebe `{ "email": "...", "senha": "..." }` e retorna `{ "token": "...", "tipo": "Bearer" }`. Senha inválida e usuário inexistente usam mensagem genérica. |
| `GET /swagger-ui.html` | público | Redireciona para `/swagger-ui/index.html`. |
| `GET /api-docs` | público | Documento OpenAPI. |

O token deve ser enviado como `Authorization: Bearer <JWT>`.

## Salas, professores e materiais

As consultas e filtros estão detalhados em [api-recursos.md](api-recursos.md). Os endpoints de criação e consulta individual são:

| Método e caminho | Perfil | Corpo/resultado |
|---|---|---|
| `POST /api/salas` | `ADMIN` | Corpo: `nome`, `tipo`, `capacidade`, `localizacao`, `restrita`, `ativa`. |
| `GET /api/salas` | autenticado | Lista e filtros de sala. |
| `GET /api/salas/{id}` | autenticado | Consulta uma sala; inexistente retorna `404`. |
| `POST /api/professores` | `ADMIN` | Corpo: `nome`, `email`, `competencia`, `ativo`. |
| `GET /api/professores` | autenticado | Lista e filtra por competência/disponibilidade. |
| `GET /api/professores/{id}` | autenticado | Consulta um professor. |
| `POST /api/materiais` | `ADMIN` | Corpo: `nome`, `tipo`, `quantidadeTotal`, `quantidadeDisponivel`, `localizacao`, `restrito`, `ativo`. |
| `GET /api/materiais` | autenticado | Lista e filtra por tipo/localização/disponibilidade. |
| `GET /api/materiais/{id}` | autenticado | Consulta um material. |

Os campos obrigatórios são validados. A capacidade da sala deve ser positiva; quantidades de material não podem ser negativas e `quantidadeDisponivel` não pode exceder `quantidadeTotal`.

## Reservas

### Criação e consulta

| Método e caminho | Perfil | Comportamento |
|---|---|---|
| `POST /api/reservas` | `SOLICITANTE` | Cria uma reserva informando exatamente um entre `salaId`, `professorId` e `materialId`, além de `inicio` e `termino`. |
| `GET /api/reservas` | autenticado | `SOLICITANTE` vê suas próprias reservas; `RESPONSAVEL` e `ADMIN` veem todas. |
| `GET /api/reservas/{id}` | autenticado | `SOLICITANTE` só consulta a própria reserva; equipe operacional pode consultar as reservas. |
| `PUT /api/reservas/{id}` | `SOLICITANTE` proprietário | Altera recurso/período da própria reserva e reavalia conflito, manutenção e aprovação. |
| `DELETE /api/reservas/{id}` | `SOLICITANTE` proprietário | Cancela somente reservas `SOLICITADA` ou `APROVADA`; reservas iniciadas/concluídas não são apagadas. |

Corpo de criação/alteração:

```json
{
  "salaId": 1,
  "professorId": null,
  "materialId": null,
  "inicio": "2026-11-04T09:00:00-03:00",
  "termino": "2026-11-04T10:00:00-03:00"
}
```

O intervalo deve ter `termino` posterior a `inicio`. Conflitos de sala, professor, material ou manutenção retornam `409` e geram auditoria de conflito evitado.

### Aprovação e operação

| Método e caminho | Perfil | Transição |
|---|---|---|
| `POST /api/reservas/{id}/aprovar` | `RESPONSAVEL` | `SOLICITADA → APROVADA` para recurso restrito, após revalidar conflitos. |
| `POST /api/reservas/{id}/rejeitar` | `RESPONSAVEL` | `SOLICITADA → REJEITADA` para recurso restrito. |
| `POST /api/reservas/{id}/retirada` | `RESPONSAVEL` ou `ADMIN` | `APROVADA → EM_USO`, registrando `retiradoEm`. |
| `POST /api/reservas/{id}/devolucao` | `RESPONSAVEL` ou `ADMIN` | `EM_USO → CONCLUIDA`, exigindo retirada e registrando `devolvidoEm`. |
| `POST /api/reservas/{id}/nao-compareceu` | `RESPONSAVEL` ou `ADMIN` | `APROVADA → NAO_COMPARECEU`. Chamadas repetidas retornam `409`. |

Resposta de reserva contém `id`, `solicitanteId`, IDs dos recursos, `inicio`, `termino`, `status`, `retiradoEm` e `devolvidoEm`.

## Manutenção

| Método e caminho | Perfil | Comportamento |
|---|---|---|
| `POST /api/manutencoes` | `RESPONSAVEL` ou `ADMIN` | Cria manutenção para exatamente um recurso, com `inicio`, `termino` e `motivo`. |
| `GET /api/manutencoes` | `RESPONSAVEL` ou `ADMIN` | `RESPONSAVEL` vê suas manutenções; `ADMIN` vê todas. |
| `GET /api/manutencoes/{id}` | `RESPONSAVEL` ou `ADMIN` | `RESPONSAVEL` só consulta a própria; `ADMIN` consulta qualquer uma. |
| `DELETE /api/manutencoes/{id}` | `RESPONSAVEL` proprietário ou `ADMIN` | Cancela a manutenção e libera o recurso quando aplicável. |

O controller de manutenção usa o perfil Spring `!test`: as rotas ficam disponíveis na execução normal da aplicação e são substituídas por serviços/dublês nos testes que usam o perfil `test`.

Corpo de criação:

```json
{
  "salaId": 1,
  "professorId": null,
  "materialId": null,
  "inicio": "2026-11-04T09:00:00-03:00",
  "termino": "2026-11-04T10:00:00-03:00",
  "motivo": "Manutenção preventiva"
}
```

Manutenção ativa bloqueia reservas sobrepostas e conflitos são respondidos com `409`.

## Auditoria

| Método e caminho | Perfil | Resultado |
|---|---|---|
| `GET /api/auditorias` | `RESPONSAVEL` ou `ADMIN` | Lista os eventos auditáveis. |
| `GET /api/auditoria` | `RESPONSAVEL` ou `ADMIN` | Alias compatível da rota plural. |

Os eventos persistem usuário, entidade, ação, estados anterior/posterior, detalhes estruturados e horário. Operações relevantes incluem criação, aprovação, rejeição, retirada, devolução, cancelamento, não comparecimento, manutenção e conflito evitado.

## Notificações

| Método e caminho | Perfil | Resultado |
|---|---|---|
| `GET /api/notificacoes` | qualquer autenticado | Lista somente notificações do usuário autenticado. |
| `GET /api/notificacao` | qualquer autenticado | Alias compatível da rota plural. |
| `PATCH /api/notificacoes/{id}/lida` | destinatário autenticado | Marca a notificação como lida; notificações de outro usuário são rejeitadas. |
| `PATCH /api/notificacao/{id}/lida` | destinatário autenticado | Alias compatível da rota plural. |

As notificações são internas e persistidas; não há integração HTTP externa que exija WireMock neste projeto.

## Relatórios

Os três endpoints de relatório, seus parâmetros, perfis e respostas estão em [api-relatorios.md](api-relatorios.md): utilização, carga horária e conflitos evitados.

## Referências de implementação e testes

- Controllers: `src/main/java/br/com/senac/organizacaorecursos/controller/`.
- Regras de acesso: `SecurityConfig` e serviços de domínio.
- Testes de API e PostgreSQL: `src/test/java/br/com/senac/organizacaorecursos/`.
- Estados e decisões: [ADRs](adr/) e [RTM](rtm.md).
