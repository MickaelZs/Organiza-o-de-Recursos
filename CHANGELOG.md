# Changelog

## v1.0.0-rc1

Data da decisão: 18/11/2026.

### Funcionalidades

- Gestão de salas, professores e materiais.
- Login com JWT e autorização por perfis ADMIN, RESPONSAVEL e SOLICITANTE.
- Solicitação, alteração e cancelamento de reservas.
- Aprovação/rejeição de recursos restritos.
- Retirada, devolução e registro de não comparecimento.
- Manutenção de recursos, auditoria, notificações e relatórios.
- Interface web responsiva e documentação OpenAPI/REST.

### Qualidade

- Build Maven e suíte JUnit com PostgreSQL via Testcontainers.
- Gates JaCoCo de 80% de linhas e 70% de branches.
- Pipeline GitHub Actions para PRs e `main`.
- Análise SonarCloud no PR #12.
- RTM, matriz de autorização, ADRs e diagramas da solução.
- Registro de defeitos e evidências de testes, carga e qualidade.

### Evidências de release

- Commit avaliado da integração: `58d91835400f4539b7b98f3236e0db10e19896a0`.
- Pull request integrado: [PR #12](https://github.com/MickaelZs/Organiza-o-de-Recursos/pull/12).
- CI pós-merge: [GitHub Actions 44409108743](https://github.com/MickaelZs/Organiza-o-de-Recursos/actions/runs/44409108743).
- Resultado local de verificação: 215 testes, 0 falhas, 0 erros e 0 ignorados.
- JaCoCo remoto no CI: LINE 89,84% e BRANCH 72,07%.

### Ressalvas

- JMeter possui planos e resultados históricos, mas não há pacote final isolado e durável para todos os cenários.
- O aceite formal por revisão individual no GitHub não consta; o registro humano disponível é o merge do PR #12.
- Os nomes civis completos e o aceite nominal individual dependem de declaração dos integrantes.

Não declarar prontidão para produção a partir desta versão candidata.
