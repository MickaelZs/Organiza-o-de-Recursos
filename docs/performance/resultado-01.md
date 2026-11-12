# Resultado JMeter 01 — auditoria final

## Identificação

- Projeto: Organização de Recursos.
- Commit técnico auditado: 3426e518d9993c5db54de088d30773fb60ac1f40.
- Plano: docs/performance/organizacao-recursos-consulta.jmx e
  docs/performance/organizacao-recursos-carga.jmx.
- Status desta execução: NÃO EXECUTADA nesta auditoria.

## Evidência encontrada

O repositório contém planos JMX e o relatório histórico
[resultado-jmeter.md](resultado-jmeter.md), que registra execuções locais de
08/11/2026 e 10/11/2026. Os artefatos brutos JTL/HTML descritos no relatório
não estão presentes no workspace ou no artifact CI, e o executável JMeter não
está instalado no ambiente auditado. Portanto, as métricas históricas não foram
recalculadas nem atribuídas ao commit 3426e51.

O workflow CI não executa JMeter. A suíte JUnit comprova concorrência e
integridade em PostgreSQL, mas não substitui uma execução de carga JMeter.

## Escopo pendente de execução

Executar em ambiente descartável e documentar, com data, commit, versão do
JMeter, configuração do PostgreSQL e os arquivos JTL/HTML sanitizados:

1. Mesma sala e mesmo horário com solicitações simultâneas; confirmar no banco
   que no máximo uma reserva conflitante ficou ativa.
2. Professor em horários sobrepostos.
3. Recursos diferentes sem conflito.
4. Intervalos adjacentes aceitos.
5. Reserva durante manutenção rejeitada.
6. Consultas concorrentes com códigos HTTP, assertions, throughput, média,
   p90, p95, p99, mínimo e máximo.

Não usar o PostgreSQL local compartilhado nem dados pessoais. O serviço
PostgreSQL existente pediu senha para conexão administrativa e não foi usado
para preparar a carga. Nenhum resultado de desempenho desta execução é
inventado.

## Referências

- [Plano JMeter](plano-jmeter.md)
- [Relatório histórico](resultado-jmeter.md)
- [Testes concorrentes PostgreSQL](../../src/test/java/br/com/senac/organizacaorecursos/ReservaConcorrenciaPostgresTest.java)
- [Testes de disputa reserva/manutenção](../../src/test/java/br/com/senac/organizacaorecursos/ManutencaoReservaConcorrenciaPostgresTest.java)
