# SonarCloud

## Configuração do workflow

O workflow executa o scanner Maven quando SONAR_TOKEN, SONAR_ORGANIZATION
e SONAR_PROJECT_KEY existem. Ele gera o relatório JaCoCo antes da análise e
usa sonar.qualitygate.wait=true; o scanner aguarda o resultado do gate.
Segredos não são versionados. O workflow restringe GITHUB_TOKEN a
contents: read e pull-requests: read.

## Análise verificada

- Pull request: [#9](https://github.com/MickaelZs/Organiza-o-de-Recursos/pull/9)
- Branch: feature/qa-finalizacao-etapas-1-2
- Commit analisado: 3426e518d9993c5db54de088d30773fb60ac1f40
- Check: [SonarCloud Code Analysis](https://sonarcloud.io/dashboard?id=MickaelZs_Organiza-o-de-Recursos&pullRequest=9), concluído com sucesso em 12/11/2026.
- Quality Gate: OK.
- Reliability Rating no código novo: A (new_reliability_rating = 1.0).
- Bugs no código novo: 0.
- Security Rating e Maintainability Rating no código novo: A.
- Duplicação no código novo: 0.0%.
- Security hotspots revisados: 100%.
- Consulta de issues abertas no período de código novo: 0.

Os dois achados registrados na solicitação que originou a correção foram
resolvidos no commit 3426e51: reutilização de SecureRandom no bootstrap e
remoção da chamada entre sobrecargas @Transactional da mesma classe na
auditoria de conflito. O registro de defeitos mantém o histórico e o escopo
desses achados.

## Limites

O resultado comprova a análise do PR #9 no commit acima. Não comprova a
aprovação de um commit em main, de uma tag ou de uma versão de produção.
O workflow pode omitir a análise se as configurações externas faltarem; nesta
execução, o check SonarCloud separado e aprovado comprova que a análise ocorreu.
