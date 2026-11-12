# Changelog

## v1.0.0-rc1 — rascunho de escopo

Estado: release candidate ainda não criado. Este conteúdo descreve o trabalho
atualmente presente na branch feature/qa-finalizacao-etapas-1-2; o commit final
da tag precisa ser o commit avaliado em main após revisão e integração.

### Funcionalidades e qualidade

- Bootstrap seguro do primeiro administrador com proteção de banco local vazio,
  Flyway, BCrypt e lock transacional PostgreSQL.
- Reservas, manutenção, aprovação de recursos restritos, retirada/devolução,
  auditoria, notificações e relatórios.
- Interface web e documentação de API, requisitos, arquitetura e operação.
- Testes unitários, API e integração PostgreSQL via Testcontainers, com gates
  JaCoCo de 80% de linhas e 70% de branches.
- GitHub Actions, publicação de relatórios de teste/JaCoCo e análise SonarCloud.

### Correções recentes na branch do PR #9

- Reutilização de SecureRandom para gerar a chave temporária do bootstrap.
- Remoção da chamada entre métodos transacionais da mesma classe no registro de
  conflitos evitados, preservando REQUIRES_NEW.
- Atualização do pacote de evidências e do parecer técnico para o estado real
  do PR e das verificações disponíveis.

### Estado das evidências

O commit 3426e51 do PR #9 tem build e SonarCloud aprovados e passou
clean verify em clone temporário. A tag e a execução CI sobre main permanecem
pendentes de revisão humana, integração e autorização do responsável.
