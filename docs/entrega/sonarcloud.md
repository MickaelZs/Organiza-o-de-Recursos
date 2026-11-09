# SonarCloud

## Configuração versionável

O workflow contém uma etapa condicional que executa o scanner Maven quando estas configurações externas existem:

- secret `SONAR_TOKEN`;
- variável `SONAR_ORGANIZATION`;
- variável `SONAR_PROJECT_KEY`.

O relatório JaCoCo XML é gerado antes da etapa e informado por `sonar.coverage.jacoco.xmlReportPaths`.

## Status da auditoria

- integração no workflow: CONFIGURADA;
- token no repositório: NÃO VERSIONADO;
- execução SonarCloud nesta auditoria: NÃO COMPROVADA;
- quality gate: DESCONHECIDO até o projeto existir/ser autorizado no SonarCloud.

É necessário cadastrar as variáveis/secret no repositório e executar um PR ou push para obter o resultado real.
