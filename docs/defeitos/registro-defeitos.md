# Registro de defeitos

## Atualização da contenção de pool — correção em validação

A pilha da JVM demonstrou espera por conexão para auditoria `REQUIRES_NEW` com a transação principal retendo conexão e lock. Auditoria de conflito agora usa a transação da operação, cuja exceção de conflito esperado não causa rollback; a verificação ocorre antes das alterações de domínio. Teste PostgreSQL com 16 concorrentes passou com uma reserva e 15 auditorias; carga HTTP e JMeter repetidas não apresentaram 500. Validação funcional completa e cenários isolados pendentes.


## Cabeçalho e boas-vindas mobile — corrigido parcialmente

Em Chromium autenticado, a página excedia 320–390 px de largura. O CSS ajusta os elementos do cabeçalho e quebra o texto de boas-vindas. Após a alteração servida diretamente ao navegador, a largura da página coincidiu com os viewports 320, 375, 390, 768, 1024 e 1440 px nos três perfis. A validação visual e funcional completa permanece pendente.

## Achado de carga 12/11/2026 — aberto, prioridade alta

Sob concorrência nos planos JMeter, quatro requisições retornaram HTTP 500 após aproximadamente 30 segundos. Uma reserva foi persistida no intervalo concorrente. Causa ainda não confirmada; investigar contenção e transações, corrigir e repetir a carga antes da liberação. [Resultado reproduzido](../performance/resultado-01.md).


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

## D-005 — Instância SecureRandom recriada no bootstrap

- Origem: alerta java:S2119 informado na revisão SonarCloud do PR #9, na
  criação da chave JWT temporária.
- Causa: uma instância criptograficamente segura era criada a cada chamada de
  bootstrap, sem necessidade.
- Correção: SecureRandom agora é mantido como campo static final e reutilizado;
  os bytes e a finalidade da chave permanecem iguais.
- Impacto: remove o alerta de confiabilidade sem enfraquecer a geração da chave.
- Testes: AdminBootstrapCommandTest e suíte completa; não houve mudança de
  comportamento funcional.
- Evidência: [AdminBootstrapCommand](../../src/main/java/br/com/senac/organizacaorecursos/cli/AdminBootstrapCommand.java),
  [check SonarCloud](https://sonarcloud.io/dashboard?id=MickaelZs_Organiza-o-de-Recursos&pullRequest=9).
- Severidade original: não recuperável na consulta pós-correção; a mensagem e
  a regra vieram do alerta da revisão.
- Status: corrigido no commit 3426e51.

## D-006 — Chamada interna entre métodos transacionais de auditoria

- Origem: alerta de requisito @Transactional incompatível na sobrecarga
  registrarConflitoEvitado do PR #9.
- Causa: método público com Map serializava os detalhes e invocava outra
  sobrecarga da própria classe. A chamada interna não atravessava o proxy Spring.
- Correção: a serialização foi movida para a implementação pública anotada com
  REQUIRES_NEW; os chamadores continuam entrando pelo proxy e a auditoria de
  conflito permanece independente do rollback da reserva/manutenção.
- Impacto: elimina a self-invocation e preserva a semântica transacional
  necessária; nenhuma propagação foi removida.
- Testes: RelatorioPostgresTest.registraEConsultaConflitoEvitadoPersistido e
  suíte PostgreSQL completa.
- Evidência: [AuditoriaService](../../src/main/java/br/com/senac/organizacaorecursos/service/AuditoriaService.java),
  [relatório de testes](../testes/relatorio-testes.md), [SonarCloud](https://sonarcloud.io/dashboard?id=MickaelZs_Organiza-o-de-Recursos&pullRequest=9).
- Severidade original: não recuperável na consulta pós-correção; a mensagem do
  alerta foi preservada na solicitação recebida.
- Status: corrigido no commit 3426e51.

## D-007 — Evidências documentais apontavam para HEAD e CI obsoletos

- Origem: auditoria final da Etapa 6 em 12/11/2026.
- Causa: alguns arquivos ainda identificavam f58a68e como HEAD e afirmavam
  que CI/SonarCloud estavam pendentes, apesar do resultado posterior no PR #9.
- Correção: relatórios, índice de evidências e status foram atualizados para
  3426e51 e para os checks remotos verificados.
- Impacto: evita apresentar evidência anterior como resultado do commit atual.
- Testes: ValidateRtm.java após a atualização documental.
- Status: corrigido nesta auditoria; pendências JMeter, visual e review continuam
  explicitamente abertas.
