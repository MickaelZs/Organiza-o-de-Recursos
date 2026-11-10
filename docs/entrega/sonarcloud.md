# SonarCloud

## Configuração versionável

O workflow contém uma etapa condicional que executa o scanner Maven quando estas configurações externas existem:

- secret `SONAR_TOKEN`;
- variável `SONAR_ORGANIZATION`;
- variável `SONAR_PROJECT_KEY`.

O relatório JaCoCo XML é gerado antes da etapa e informado por `sonar.coverage.jacoco.xmlReportPaths`. Quando as configurações existem, o scanner aguarda o resultado efetivo com `sonar.qualitygate.wait=true`; a análise não é marcada como aprovada apenas por iniciar.

O workflow também declara apenas `contents: read` e `pull-requests: read`, verifica o Docker no runner e publica os relatórios Surefire/Failsafe/JaCoCo como artifact. Se alguma configuração SonarCloud faltar, o workflow registra um aviso explícito e não executa o scanner.

## Status da auditoria

- integração no workflow: CONFIGURADA;
- token no repositório: NÃO VERSIONADO;
- execução SonarCloud para a árvore local da Etapa 2: NÃO COMPROVADA, pois não há token/variáveis locais e as alterações ainda não foram publicadas;
- quality gate da árvore local: DESCONHECIDO;
- evidência remota anterior: o check do commit `d0100a1` reportou Quality Gate passed, mas não é evidência desta árvore local não publicada.

É necessário manter `SONAR_TOKEN`, `SONAR_ORGANIZATION` e `SONAR_PROJECT_KEY` configurados no repositório e executar o workflow desta revisão por PR ou push. O resultado efetivo deve ser conferido no check do GitHub/SonarCloud, sem inferência a partir do YAML.
