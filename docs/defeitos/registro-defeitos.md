# Registro de defeitos

Somente ocorrências comprovadamente encontradas nas etapas anteriores e
confirmadas por teste, histórico ou artefato de execução estão listadas. A
severidade e a prioridade abaixo são a classificação desta auditoria, não uma
alegação de impacto em produção.

## D-001 — Redirect do Swagger documentado incorretamente

- Severidade: S3 — baixa; não impede o uso da API, mas quebra o fluxo documentado.
- Prioridade: P2 — corrigir antes da entrega final.
- Descrição: o teste/documentação esperava HTTP 200 em `/swagger-ui.html`, mas o Springdoc redireciona para `/swagger-ui/index.html`.
- Reprodução: executar `GET /swagger-ui.html` no teste de integração; observar redirect em vez de 200 direto.
- Correção: o teste passou a validar o redirect, o header `Location` e o endpoint final.
- Teste de regressão: `InterfaceErrosIntegrationTest.disponibilizaInterfaceEAssetsSemAbrirRotasDaApi`.
- Evidência: [teste](../../src/test/java/br/com/senac/organizacaorecursos/InterfaceErrosIntegrationTest.java), [contrato de erros](../api-erros.md).
- Status: Corrigido; coberto na última execução local bem-sucedida registrada.

## D-002 — Cancelamento de reserva em estado já iniciado ou concluído

- Severidade: S1 — alta; poderia apagar a validade operacional do histórico.
- Prioridade: P1 — correção obrigatória antes da entrega.
- Descrição: `EM_USO` e `CONCLUIDA` podiam ser alteradas para `CANCELADA`.
- Reprodução: criar uma reserva, levá-la a `EM_USO` ou `CONCLUIDA` e chamar `DELETE /api/reservas/{id}`.
- Correção: `Reserva.cancelar()` e o serviço restringem o cancelamento a `SOLICITADA` ou `APROVADA`, retornando 409 para estados iniciados/concluídos.
- Teste de regressão: `ReservaApiPostgresTest.naoPermiteCancelarReservaJaIniciadaOuConcluida`.
- Evidência: [teste](../../src/test/java/br/com/senac/organizacaorecursos/ReservaApiPostgresTest.java), [ADR de estados](../adr/adr-0002-regra-conflito-reservas.md).
- Status: Corrigido; coberto na última execução PostgreSQL bem-sucedida registrada.

## D-003 — Alteração de reserva sem auditoria

- Severidade: S1 — alta; perda de rastreabilidade de alteração é incompatível com RC-06.
- Prioridade: P1 — correção obrigatória antes da entrega.
- Descrição: `ReservaService.atualizar()` persistia a alteração sem criar evento `ALTERADA` com os estados e o recurso envolvidos.
- Reprodução: atualizar uma reserva própria e consultar a auditoria da entidade; não havia registro correspondente à alteração.
- Correção: o serviço registra estado anterior/posterior e snapshots estruturados antes/depois da operação, sem incluir segredos.
- Teste de regressão: `AuditoriaPostgresTest.registraAlteracaoDeReservaNoHistorico`.
- Evidência: [teste](../../src/test/java/br/com/senac/organizacaorecursos/AuditoriaPostgresTest.java), [serviço de auditoria](../../src/main/java/br/com/senac/organizacaorecursos/service/AuditoriaService.java).
- Status: Corrigido; coberto na última execução PostgreSQL bem-sucedida registrada.

## D-004 — Documentação de disponibilidade desatualizada sobre manutenção

- Severidade: S2 — média; poderia induzir uso de um recurso bloqueado.
- Prioridade: P1 — corrigir antes da validação final.
- Descrição: `docs/api-recursos.md` dizia que a disponibilidade não considerava manutenção, em divergência com `DisponibilidadeSpecifications`.
- Reprodução: comparar a documentação com a busca temporal e o teste que cria uma manutenção ativa; o comportamento real exclui o recurso.
- Correção: a documentação foi alinhada ao comportamento implementado e ao intervalo semiaberto da busca.
- Teste de regressão: `ResourceSearchApiPostgresTest.filtraDisponibilidadeDeSalaAntesDepoisEOcupacaoNoIntervaloConflitante` e `ManutencaoApiRedTest`.
- Evidência: [API de recursos](../api-recursos.md), [teste de busca](../../src/test/java/br/com/senac/organizacaorecursos/ResourceSearchApiPostgresTest.java), [especificação oficial](https://afonsolelis.github.io/aulas_senac/pages/qualidade2/especificacao-projeto.html).
- Status: Corrigido documentalmente; comportamento confirmado pela reexecução PostgreSQL de 12/11/2026.

## Observação sobre JMeter

As execuções preliminares de carga registraram 401 quando o token não estava
disponível e 500/timeouts em um cenário combinado. Esses resultados são
mantidos como limitação de evidência em [resultado-jmeter.md](../performance/resultado-jmeter.md),
mas não foram promovidos a defeito funcional sem causa reproduzível isolada:
as execuções finais dos cenários individuais passaram pelas assertions e o
plano foi ajustado para não expor tokens. Não foi criado um D-005 especulativo.
