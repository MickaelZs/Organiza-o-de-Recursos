# Registro de defeitos

Somente defeitos encontrados durante a auditoria estão listados.

| ID | Sintoma | Causa | Correção | Status |
|---|---|---|---|---|
| D-001 | `InterfaceErrosIntegrationTest` esperava 200 em `/swagger-ui.html` | `SwaggerWelcomeWebMvc` redireciona para `/swagger-ui/index.html` | Teste passou a validar 302, `Location` e endpoint final | Corrigido |
| D-002 | Cancelamento podia mudar uma reserva `EM_USO` ou `CONCLUIDA` para `CANCELADA` | `Reserva.cancelar()` não restringia estados e o serviço não validava a transição | Cancelamento agora aceita somente `SOLICITADA` ou `APROVADA`, com 409 e teste parametrizado | Corrigido |
| D-003 | Alteração de reserva não entrava no histórico de auditoria | `ReservaService.atualizar()` persistia a alteração sem chamar `AuditoriaService` | Registro `ALTERADA` com estado anterior/posterior e teste PostgreSQL | Corrigido |
| D-004 | Documentação dizia que disponibilidade não considerava manutenção | `api-recursos.md` estava desatualizado em relação a `DisponibilidadeSpecifications` | Documentação alinhada ao comportamento real | Corrigido |

Não foi encontrado cliente HTTP externo. Portanto, a ausência de `WireMockServer`, `WireMockExtension` e `stubFor` não é defeito funcional do RF11.
