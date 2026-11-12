# Organização de Recursos

Projeto acadêmico da disciplina de Qualidade de Software para gerenciamento de salas, professores, materiais/equipamentos, reservas, manutenção, retirada/devolução, auditoria, notificações e relatórios.

## Tecnologias

- Java 21 e Spring Boot 3.5;
- Spring Web, Data JPA, Validation e Security com JWT;
- PostgreSQL e Flyway;
- JUnit 5, Mockito e testes parametrizados com Testcontainers/PostgreSQL;
- JaCoCo e Springdoc/OpenAPI.

## Pré-requisitos e configuração

O passo a passo completo está no [guia de instalação](docs/guia-instalacao.md).

- Java 21;
- Docker Desktop em execução para os testes PostgreSQL/Testcontainers;
- PostgreSQL para executar a aplicação fora dos testes.

Crie um banco `organizacao_recursos` e configure, sem versionar segredos:

```text
DB_URL=jdbc:postgresql://localhost:5432/organizacao_recursos
DB_USERNAME=postgres
DB_PASSWORD=sua-senha-local
JWT_SECRET=segredo-fornecido-pelo-ambiente
JWT_EXPIRATION=PT2H
```

`JWT_SECRET` é obrigatório e deve ser fornecido pelo ambiente em Base64; a aplicação não possui fallback de segredo. `.env.example` serve apenas como referência com um valor falso.

## Executar

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Health check: `GET /api/health`. A especificação OpenAPI fica disponível em `/swagger-ui.html`.
Interface web responsiva: `/`.

## Qualidade e arquitetura

Consulte a [RTM](docs/rtm.md), a [análise ATAM preliminar](docs/atam.md) e o
[relatório de qualidade](docs/relatorio-qualidade.md) para rastreabilidade,
cenários e evidências da auditoria.

### Primeiro acesso ao sistema

Este procedimento cria o primeiro ADMIN apenas em um banco PostgreSQL local novo. O comando recusa URLs remotas, URLs com credenciais/parametrização e bancos que já tenham objetos de aplicação; não remova dados para contornar essa proteção.

Pré-requisitos: Java 21, Maven Wrapper, PostgreSQL local em execução e permissão para criar/usar um banco vazio. No PowerShell, crie o banco vazio (ajuste `psql`/credenciais conforme sua instalação):

```powershell
createdb -h localhost -p 5432 -U postgres organizacao_recursos
$env:DB_URL = 'jdbc:postgresql://localhost:5432/organizacao_recursos'
$env:DB_USERNAME = 'postgres'
.\mvnw.cmd -B -DskipTests package
java -jar .\target\organizacao-recursos-0.0.1-SNAPSHOT.jar --bootstrap-admin
```

O comando solicita interativamente o nome, e-mail, senha do ADMIN e senha do PostgreSQL. As senhas são digitadas sem eco; a senha do administrador precisa ter ao menos 12 caracteres. Não passe senhas como argumentos nem as grave em arquivos versionados. O comando verifica que o banco não contém objetos de aplicação antes de iniciar o Spring/Flyway. As migrations são aplicadas automaticamente somente após essa verificação; a transação e um lock advisory PostgreSQL impedem duas inicializações simultâneas e qualquer banco que já tenha usuário é preservado sem alterações pelo serviço.

Após a mensagem de sucesso, configure as credenciais normais da aplicação. Gere um segredo JWT local aleatório em Base64 e informe a senha do banco como variável de ambiente:

```powershell
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
try {
    $rng.GetBytes($bytes)
}
finally {
    $rng.Dispose()
}
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
[Array]::Clear($bytes, 0, $bytes.Length)
$env:JWT_EXPIRATION = 'PT2H'
$secureDbPassword = Read-Host 'Senha do PostgreSQL' -AsSecureString
$dbPasswordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secureDbPassword)
try {
    $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($dbPasswordPointer)
}
finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($dbPasswordPointer)
}
.\mvnw.cmd spring-boot:run
```

Abra `http://localhost:8080/` e entre com o e-mail e a senha escolhidos no bootstrap. O segredo JWT precisa ser fornecido novamente sempre que iniciar o backend; não o compartilhe nem o versione.

Erros comuns: confirme que `DB_URL` aponta para `localhost` ou `127.0.0.1`, contém porta e nome do banco, e não inclui credenciais; confirme `DB_USERNAME` e a senha do PostgreSQL. Se o comando disser que o banco não está vazio ou que já existe usuário, interrompa e selecione/crie um banco local novo — não apague tabelas ou usuários para repetir o processo. Se as senhas digitadas não coincidirem, execute novamente apenas se o banco continuar vazio. A execução requer terminal interativo para impedir que a senha seja exibida.

## Fluxos e endpoints principais

- Autenticação: `POST /api/auth/login`;
- Recursos: `/api/salas`, `/api/professores` e `/api/materiais`;
- Reservas: `/api/reservas`, aprovação/rejeição, retirada/devolução e registro operacional de não comparecimento (`POST /api/reservas/{id}/nao-compareceu`);
- Manutenção: `/api/manutencoes`;
- Auditoria: `GET /api/auditorias`;
- Notificações: `GET /api/notificacoes` e `PATCH /api/notificacoes/{id}/lida`;
- Relatórios: `GET /api/relatorios/utilizacao`, `/carga-horaria` e `/conflitos-evitados`.

O catálogo completo de endpoints, perfis, payloads e transições está em [docs/api-fluxos.md](docs/api-fluxos.md).

Os relatórios exigem `inicio` e `termino` em ISO-8601 com offset e usam o intervalo semiaberto `[inicio, termino)`. RESPONSAVEL e ADMIN podem consultá-los. Utilização detalha cada recurso e carga horária detalha professores alocados, sem incluir estados não efetivados. A especificação detalhada está em [docs/api-relatorios.md](docs/api-relatorios.md).

## Perfis e segurança

- `SOLICITANTE`: cria/altera/cancela as próprias reservas e consulta as próprias notificações;
- `RESPONSAVEL`: aprova/rejeita reservas restritas, opera manutenção, registra retirada/devolução, consulta auditoria e relatórios;
- `ADMIN`: operações administrativas, retirada/devolução, auditoria e relatórios.

Senhas são armazenadas com BCrypt e não são serializadas. A API é stateless com JWT. Rotas não autenticadas são somente health e login.

## Tratamento de erros

Respostas de erro da API seguem `timestamp`, `status`, `error`, `message` e `path`. Os códigos principais são 400 para entrada inválida, 401 para ausência de autenticação, 403 para falta de permissão, 404 para recurso inexistente e 409 para conflito/regra de estado. Falhas inesperadas retornam 500 sem stack trace. Detalhes estão em [docs/api-erros.md](docs/api-erros.md).

## Migrations

Flyway aplica, em ordem, V1 a V10. V8 cria os campos de retirada/devolução, V9 cria auditorias e V10 cria notificações. RF12, RF13 e RF14 não exigem nova tabela nem V11.

## Testes e qualidade

```bash
./mvnw -B test
./mvnw -B clean verify
java scripts/ValidateRtm.java
```

No Windows PowerShell, use:

```powershell
.\mvnw.cmd -B test
.\mvnw.cmd -B clean verify
java scripts\ValidateRtm.java
```

Os testes críticos de persistência, concorrência e relatórios usam PostgreSQL real por Testcontainers; não há substituição por H2. O verificador da RTM roda após a suíte para validar links, IDs, fontes, testes, evidências e relatórios Surefire das linhas marcadas `PASS`.

Última validação do commit 3426e518d9993c5db54de088d30773fb60ac1f40 (12/11/2026): clone temporário limpo, Java 21.0.11, Docker Engine 29.9.2 e PostgreSQL 16.15 via Testcontainers; clean verify passou com 214 testes, 0 falhas, 0 erros e 0 ignorados. JaCoCo local: 90,01% LINE e 72,07% BRANCH. O artifact da CI para o mesmo commit reportou 89,84% LINE e 72,07% BRANCH; ambos passam 80/70. GitHub Actions e SonarCloud também passaram no PR #9. Veja o [relatório de testes](docs/testes/relatorio-testes.md) e o [índice de evidências](docs/evidencias/README.md).

Os resultados históricos de etapas anteriores permanecem preservados no [relatório de testes](docs/testes/relatorio-testes.md): registro `d0100a1` (`main`, 08/11/2026) com 192 testes e registro `f58a68e` (09/11/2026) com 204 testes. Eles não devem ser confundidos com os 214 testes da árvore de trabalho auditada em 12/11/2026.

## Estrutura

O código fica em `src/main/java/br/com/senac/organizacaorecursos`, organizado em `config`, `controller`, `dto`, `entity`, `exception`, `repository`, `security` e `service`. Requisitos, decisões e rastreabilidade ficam em `docs/`.

Interface: o sistema disponibiliza uma interface web estática em `/` com acesso por JWT e painéis condicionados ao perfil. A inspeção visual manual em resoluções/dispositivos permanece pendente; mensagens de erro são apresentadas sem expor stack traces, hashes ou segredos. O redirect documentado pelo Springdoc é `/swagger-ui.html` → `/swagger-ui/index.html`.

## Qualidade e evidências

- [Matriz de rastreabilidade](docs/rtm.md) e [status dos requisitos](docs/status-requisitos.md);
- [Plano e relatório de testes](docs/testes/plano-testes.md) e [relatório final de testes](docs/testes/relatorio-testes.md);
- [Diagramas versionáveis](docs/diagramas/arquitetura.md), [registro de defeitos](docs/defeitos/registro-defeitos.md) e [índice de evidências](docs/evidencias/README.md);
- [Plano JMeter](docs/performance/plano-jmeter.md) e [resultado de performance](docs/performance/resultado-jmeter.md);
- [Resultado JMeter da auditoria](docs/performance/resultado-01.md);
- [Checklist da entrega](docs/entrega/checklist.md), [changelog](CHANGELOG.md) e [decisão de qualidade](docs/decisao-qualidade.md);
- [Guia de prova oral](docs/prova-oral/guia.md) e [relatório final de qualidade](docs/relatorio-qualidade.md).
