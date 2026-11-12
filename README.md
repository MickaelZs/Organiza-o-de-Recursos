# Organização de Recursos

Sistema acadêmico da disciplina de **Qualidade de Software** para organizar o uso de salas, professores e materiais. A aplicação permite consultar disponibilidade, solicitar reservas e acompanhar sua operação, mantendo regras de conflito, permissões por perfil e histórico auditável.

## Sumário

- [Funcionalidades](#funcionalidades)
- [Tecnologias e arquitetura](#tecnologias-e-arquitetura)
- [Pré-requisitos](#pré-requisitos)
- [Instalação no Windows](#instalação-no-windows)
- [Instalação no Linux e GitHub Codespaces](#instalação-no-linux-e-github-codespaces)
- [Primeiro acesso e execução](#primeiro-acesso-e-execução)
- [API](#api)
- [Testes e qualidade](#testes-e-qualidade)
- [Testes de carga com JMeter](#testes-de-carga-com-jmeter)
- [Documentação](#documentação)
- [Solução de problemas](#solução-de-problemas)
- [Equipe, contribuição e entrega](#equipe-contribuição-e-entrega)

## Funcionalidades

| Área | Operações |
|---|---|
| Acesso | Login com JWT e permissões para `ADMIN`, `RESPONSAVEL` e `SOLICITANTE`. |
| Recursos | Cadastro e consulta de salas, professores e materiais; filtros por atributos e disponibilidade. |
| Reservas | Criação, consulta, alteração e cancelamento das próprias reservas; conflitos de horário e proteção contra solicitações simultâneas para o mesmo recurso. |
| Operação | Aprovação ou rejeição de recursos restritos, manutenção, retirada, devolução e registro de não comparecimento. |
| Acompanhamento | Auditoria das operações, notificações internas e relatórios de utilização, carga horária e conflitos evitados. |

O `ADMIN` cadastra recursos e pode operar manutenção, retirada e devolução. O `RESPONSAVEL` aprova ou rejeita solicitações restritas e realiza operações de manutenção; ambos consultam auditoria e relatórios. O `SOLICITANTE` gerencia as próprias reservas e notificações. As regras completas estão na [matriz de autorização](docs/seguranca/matriz-autorizacao.md) e no [catálogo de fluxos](docs/api-fluxos.md).

## Tecnologias e arquitetura

| Camada ou atividade | Tecnologia |
|---|---|
| Aplicação | Java 21, Spring Boot 3.5.6, Spring Web, Spring Data JPA e Bean Validation |
| Segurança | Spring Security, JWT e BCrypt |
| Persistência | PostgreSQL e migrations Flyway |
| Interface | HTML, CSS e JavaScript |
| Build e testes | Maven Wrapper, JUnit 5, Mockito, Testcontainers e JaCoCo |
| Carga e integração contínua | Apache JMeter, GitHub Actions e SonarCloud |
| Documentação da API | Springdoc/OpenAPI |

A interface estática consome a API HTTP. Controllers recebem e validam as requisições; services aplicam autorização e regras de negócio; repositories acessam as entidades no PostgreSQL. Flyway controla o esquema e o Spring Security protege as rotas. Veja os [diagramas de contexto, componentes e sequências](docs/diagramas/arquitetura.md), a [arquitetura](docs/arquitetura.md) e as [decisões arquiteturais](docs/adr/).

## Pré-requisitos

- Git e **JDK 21**. Confirme `java -version` e ajuste `JAVA_HOME` e `PATH` se necessário.
- Docker com daemon ativo; no Windows, inicie o Docker Desktop. O Docker também é necessário para os testes com Testcontainers.
- Maven é fornecido pelo Wrapper (`mvnw`/`mvnw.cmd`); não é necessário instalá-lo separadamente. Na primeira execução, ele pode baixar a distribuição e dependências.
- Uma porta local livre para o PostgreSQL (os exemplos usam `55432`) e a porta `8080` para a aplicação. No Linux, o exemplo usa `openssl` para gerar o segredo JWT.

Os exemplos criam **um banco novo** em um container separado, sem recriar ou apagar bancos existentes. Escolha outro nome de container ou porta se já estiverem em uso. O banco inicial é criado pela imagem oficial na primeira inicialização; não reutilize um volume com dados existentes para repetir o bootstrap.

## Instalação no Windows

Em PowerShell, clone o repositório e entre na pasta:

```powershell
git clone https://github.com/MickaelZs/Organiza-o-de-Recursos.git
cd Organiza-o-de-Recursos
# Para avaliar a branch de trabalho, quando necessário:
git switch feature/qa-entrega-final
java -version
docker version
```

Crie o PostgreSQL 16. Digite uma senha nova quando solicitado; ela não aparece na tela. Os valores definidos neste terminal são usados pelo Docker e pela aplicação:

```powershell
$senhaSegura = Read-Host 'Crie uma senha para o PostgreSQL local' -AsSecureString
$ponteiro = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($senhaSegura)
try { $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ponteiro) }
finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ponteiro) }
$env:POSTGRES_PASSWORD = $env:DB_PASSWORD
$env:DB_USERNAME = 'postgres'
$env:DB_URL = 'jdbc:postgresql://127.0.0.1:55432/organizacao_recursos'
docker run -d --name organizacao-postgres -p 127.0.0.1:55432:5432 -e POSTGRES_USER=postgres -e POSTGRES_DB=organizacao_recursos -e POSTGRES_PASSWORD postgres:16
```

Aguarde o banco ficar pronto; repita o comando de prontidão até retornar sucesso:

```powershell
docker exec organizacao-postgres pg_isready -U postgres -d organizacao_recursos
.\mvnw.cmd -B -DskipTests package
java -jar .\target\organizacao-recursos-0.0.1-SNAPSHOT.jar --bootstrap-admin
```

No bootstrap interativo, informe nome, e-mail e uma senha nova para o ADMIN, confirme essa senha e informe a senha do PostgreSQL criada acima. A senha do ADMIN deve ter pelo menos 12 caracteres. Aguarde a mensagem de criação antes de iniciar o servidor normal.

Gere um segredo JWT aleatório em Base64 e inicie a aplicação:

```powershell
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
[Array]::Clear($bytes, 0, $bytes.Length)
$env:JWT_EXPIRATION = 'PT2H'
.\mvnw.cmd spring-boot:run
```

## Instalação no Linux e GitHub Codespaces

Em Bash, clone e confira as ferramentas:

```bash
git clone https://github.com/MickaelZs/Organiza-o-de-Recursos.git
cd Organiza-o-de-Recursos
# Para avaliar a branch de trabalho, quando necessário:
git switch feature/qa-entrega-final
java -version
docker version
```

Se houver mais de um JDK, selecione o JDK 21 disponível no ambiente, por exemplo `export JAVA_HOME=/caminho/para/jdk-21` e `export PATH="$JAVA_HOME/bin:$PATH"`; confirme novamente `java -version`. No Codespaces, use o terminal do workspace e confirme que o Docker está disponível.

Crie uma senha local sem eco e inicie um PostgreSQL 16 isolado:

```bash
read -r -s -p 'Crie uma senha para o PostgreSQL local: ' DB_PASSWORD; printf '\n'
export DB_PASSWORD POSTGRES_PASSWORD="$DB_PASSWORD" DB_USERNAME=postgres
export DB_URL='jdbc:postgresql://127.0.0.1:55432/organizacao_recursos'
docker run -d --name organizacao-postgres -p 127.0.0.1:55432:5432 -e POSTGRES_USER=postgres -e POSTGRES_DB=organizacao_recursos -e POSTGRES_PASSWORD postgres:16
```

Aguarde o banco ficar pronto; repita a verificação até retornar sucesso. Em seguida, compile e execute o bootstrap **antes** da inicialização normal:

```bash
docker exec organizacao-postgres pg_isready -U postgres -d organizacao_recursos
./mvnw -B -DskipTests package
java -jar ./target/organizacao-recursos-0.0.1-SNAPSHOT.jar --bootstrap-admin
```

Informe interativamente nome, e-mail e senha nova do ADMIN, confirme a senha e informe a senha do PostgreSQL. A senha do ADMIN deve ter pelo menos 12 caracteres. Após a mensagem de sucesso, gere o segredo JWT e inicie o servidor:

```bash
export JWT_SECRET="$(openssl rand -base64 32)"
export JWT_EXPIRATION='PT2H'
./mvnw spring-boot:run
```

No Codespaces, abra a aba **Ports** e acesse a porta encaminhada `8080` mantendo sua visibilidade **Private**. Não torne a porta do PostgreSQL pública. Para configurar um banco já existente, consulte o [guia de instalação](docs/guia-instalacao.md).

## Primeiro acesso e execução

O bootstrap aceita somente URL JDBC PostgreSQL local explícita, com porta e nome do banco, sem credenciais ou parâmetros na URL. Ele verifica se o banco está vazio antes de aplicar as migrations e criar o primeiro ADMIN. Se houver tabelas ou usuários, pare e use um banco novo; não remova dados para forçar o processo. Não há credenciais padrão de aplicação.

Com o servidor em execução, abra `http://localhost:8080/` ou a URL privada encaminhada pelo Codespaces. Entre com o e-mail e a senha escolhidos no bootstrap. Em outro terminal, verifique:

```bash
curl http://localhost:8080/api/health
```

No PowerShell, também é possível usar `Invoke-RestMethod http://localhost:8080/api/health`. A aplicação aplica as migrations Flyway e valida o esquema na inicialização. Para encerrá-la, pressione `Ctrl+C` no terminal do servidor. Para parar o container criado pelos exemplos, use `docker stop organizacao-postgres`; para retomá-lo, `docker start organizacao-postgres`. Isso preserva o container e seus dados. Ao abrir um novo terminal, forneça novamente `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e `JWT_SECRET` antes de iniciar a aplicação. Mantenha segredos fora do Git e de registros compartilhados.

## API

- Interface OpenAPI: `http://localhost:8080/swagger-ui.html`.
- Documento OpenAPI: `http://localhost:8080/api-docs`.
- `GET /api/health` e `POST /api/auth/login` são públicos; a interface e a documentação também são acessíveis sem autenticação. As rotas de negócio exigem `Authorization: Bearer <token>` após o login. Não compartilhe nem versione o token.

| Endpoint | Uso principal |
|---|---|
| `/api/salas`, `/api/professores`, `/api/materiais` | Cadastro, consulta e filtros de recursos. |
| `/api/reservas` | Criar e consultar reservas; `PUT` e `DELETE` em `/{id}` alteram e cancelam. |
| `/api/reservas/{id}/aprovar`, `/rejeitar`, `/retirada`, `/devolucao`, `/nao-compareceu` | Decisões e transições operacionais. |
| `/api/manutencoes` | Criar, consultar e cancelar manutenções. |
| `/api/auditorias`, `/api/notificacoes` | Consultar histórico e notificações; `PATCH /api/notificacoes/{id}/lida` marca leitura. |
| `/api/relatorios/utilizacao`, `/carga-horaria`, `/conflitos-evitados` | Relatórios para o período informado. |

Respostas incluem `200`/`201` para operações bem-sucedidas, `204` para cancelamentos sem corpo, `400` para dados inválidos, `401` para autenticação ausente ou inválida, `403` para acesso negado, `404` para registro inexistente e `409` para conflito ou estado inválido. Veja os [fluxos e payloads](docs/api-fluxos.md), [filtros](docs/api-recursos.md), [relatórios](docs/api-relatorios.md) e [contrato de erros](docs/api-erros.md).

## Testes e qualidade

Com Java 21 e Docker ativos, execute a suíte completa e depois valide a rastreabilidade, pois o validador usa os relatórios da execução:

```bash
./mvnw -B clean verify
java scripts/ValidateRtm.java
```

No PowerShell:

```powershell
.\mvnw.cmd -B clean verify
java scripts/ValidateRtm.java
```

Os relatórios Surefire ficam em `target/surefire-reports/` e o JaCoCo em `target/site/jacoco/index.html` e `target/site/jacoco/jacoco.xml`. Os testes de integração usam PostgreSQL via Testcontainers. O `verify` exige cobertura mínima de **80% de linhas** e **70% de branches**.

Na validação local de 12/11/2026 do commit `2494ede` da branch de trabalho, foram registrados **215 testes, zero falhas, zero erros e zero ignorados**, com JaCoCo de **89,84% de linhas** e **72,07% de branches**. A RTM estrutural passou; o build e o Quality Gate do SonarCloud também foram aprovados para aquele commit no [PR #10](https://github.com/MickaelZs/Organiza-o-de-Recursos/pull/10). Esses números identificam aquela execução; alterações posteriores devem ser verificadas novamente. Consulte o [índice de evidências](docs/evidencias/README.md) e os checks do commit avaliado.

## Testes de carga com JMeter

O [plano de execução](docs/performance/plano-jmeter.md) descreve os pré-requisitos e comandos não gráficos para JMeter 5.6.3. Os planos são [consulta](docs/performance/organizacao-recursos-consulta.jmx), [carga e concorrência](docs/performance/organizacao-recursos-carga.jmx) e [concorrência de material](docs/performance/organizacao-recursos-material.jmx). Use apenas PostgreSQL descartável e dados sintéticos: os cenários gravam reservas e manutenções. Forneça os tokens de teste por variáveis de ambiente, conforme o plano, sem colocá-los em argumentos, logs ou evidências.

O [resultado da auditoria](docs/performance/resultado-01.md) apresenta ambiente, amostras, métricas e limites; as [amostras sanitizadas](docs/evidencias/jmeter-2026-11-12/) preservam os resultados observados. Respostas `409` previstas de conflito são distintas de erros técnicos. Os testes não estabelecem capacidade de produção.

## Documentação

- [Requisitos e rastreabilidade (RTM)](docs/rtm.md), [diagramas](docs/diagramas/arquitetura.md) e [ATAM](docs/atam.md).
- [Plano de testes](docs/testes/plano-testes.md), [relatório de testes](docs/testes/relatorio-testes.md) e [evidência dos fluxos de interface](docs/evidencias/interface-fluxos-2026-11-12.md).
- [Registro de defeitos](docs/defeitos/registro-defeitos.md), [relatório de qualidade](docs/relatorio-qualidade.md) e [relatório final](docs/entrega/relatorio-final-qualidade.md).
- [Checklist de entrega](docs/entrega/checklist.md), [índice de evidências](docs/evidencias/README.md), [decisão de qualidade](docs/decisao-qualidade.md) e [CHANGELOG](CHANGELOG.md).
- [Guia de prova oral e demonstração](docs/prova-oral/guia.md) e [guia de instalação](docs/guia-instalacao.md).

## Solução de problemas

| Sintoma | Verificação |
|---|---|
| Java diferente de 21 | Confira `java -version`, `JAVA_HOME` e `PATH`; execute o Wrapper novamente com o JDK 21 ativo. |
| Docker ou Testcontainers indisponível | Inicie o daemon e confira `docker version`; o usuário precisa ter acesso ao Docker. |
| PostgreSQL não conecta | Verifique `docker ps`, `pg_isready`, a porta publicada, `DB_URL`, usuário e senha. Não recrie um banco existente para diagnosticar. |
| `JWT_SECRET` ausente ou inválido | Gere novamente um segredo forte em Base64, exporte-o no terminal do servidor e reinicie a aplicação. Tokens antigos deixarão de ser válidos se o segredo mudar. |
| Bootstrap recusado | Use terminal interativo e um banco local novo e vazio; confira URL, usuário e senha. O bootstrap não deve ser repetido sobre um banco já inicializado. |
| Porta 8080 ocupada | Encerre somente o processo que você iniciou ou configure outra porta, por exemplo com `--server.port=8081` ao iniciar a aplicação. |
| Mockito/Byte Buddy falha no sandbox | Confira a causa no relatório Surefire. Restrições de attach da JVM ou acesso ao Docker podem impedir testes no sandbox; execute no terminal autorizado do ambiente, sem desabilitar testes. |
| HTTP 401 ou 403 | Verifique login, validade do token e perfil exigido pela operação na [matriz de autorização](docs/seguranca/matriz-autorizacao.md). |

## Equipe, contribuição e entrega

Os nomes completos e as contribuições individuais devem ser confirmados pelos integrantes; não são presumidos aqui. O histórico Git registra as alterações. Para contribuir, trabalhe em uma branch, faça commits descritivos, abra um pull request para revisão e aguarde a CI e os checks de qualidade. Consulte o [PR #10](https://github.com/MickaelZs/Organiza-o-de-Recursos/pull/10) e o [workflow](.github/workflows/ci.yml); a análise SonarCloud no workflow depende das credenciais e variáveis configuradas no repositório.

A tag `v1.0.0-rc1` só deve ser criada após revisão e aprovação da equipe, integração autorizada em `main`, CI e Quality Gate aprovados no commit exato da `main` e conferência do checklist e das evidências. A [decisão de qualidade](docs/decisao-qualidade.md) permanece disponível para revisão e assinaturas reais; não pressupõe aprovação nem publicação do release.
