# Guia de instalação

Este guia descreve a instalação local do Organização de Recursos a partir da raiz do repositório. O procedimento usa PostgreSQL local para executar a aplicação e Docker Desktop para os testes com Testcontainers.

## 1. Pré-requisitos

- Git;
- Java 21;
- PostgreSQL local em execução;
- Docker Desktop em execução para `test`/`verify`;
- terminal interativo para o primeiro cadastro do administrador.

Confirme o Java:

```powershell
java -version
```

O Maven Wrapper do projeto já está versionado; não é necessário instalar Maven separadamente.

## 2. Obter o projeto

Clone o repositório fornecido pela equipe e entre na pasta do projeto. Não inclua senhas, tokens ou arquivos `.env` no Git.

PowerShell:

```powershell
git clone https://github.com/MickaelZs/Organiza-o-de-Recursos.git
Set-Location '.\Organiza-o-de-Recursos'
```

Linux/macOS:

```bash
git clone https://github.com/MickaelZs/Organiza-o-de-Recursos.git
cd 'Organiza-o-de-Recursos'
```

O comando clona o repositório público do projeto e cria uma pasta com o nome da organização do GitHub.

## 3. Criar um banco local vazio

Crie o banco antes do primeiro acesso. O bootstrap do ADMIN aceita somente um banco PostgreSQL local novo e verifica se não existem objetos da aplicação.

PowerShell:

```powershell
createdb -h localhost -p 5432 -U postgres organizacao_recursos
$env:DB_URL = 'jdbc:postgresql://localhost:5432/organizacao_recursos'
$env:DB_USERNAME = 'postgres'
```

Linux/macOS:

```bash
createdb -h localhost -p 5432 -U postgres organizacao_recursos
export DB_URL='jdbc:postgresql://localhost:5432/organizacao_recursos'
export DB_USERNAME='postgres'
```

Se o banco já tiver tabelas ou usuários da aplicação, pare e escolha outro banco vazio. Não apague dados para repetir o bootstrap.

## 4. Criar o primeiro ADMIN

Compile o projeto e execute o comando interativo:

Windows:

```powershell
.\mvnw.cmd -B -DskipTests package
java -jar .\target\organizacao-recursos-0.0.1-SNAPSHOT.jar --bootstrap-admin
```

Linux/macOS:

```bash
./mvnw -B -DskipTests package
java -jar ./target/organizacao-recursos-0.0.1-SNAPSHOT.jar --bootstrap-admin
```

O comando solicita nome, e-mail, senha do ADMIN e senha do PostgreSQL. A senha do ADMIN deve ter pelo menos 12 caracteres e não deve ser passada como argumento. O Flyway aplica as migrations somente depois da verificação de banco vazio; as versões atuais são V1–V10.

## 5. Configurar e iniciar a aplicação

Defina `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` e, opcionalmente, `JWT_EXPIRATION` na sessão do terminal. `JWT_SECRET` é obrigatório, deve ser um valor Base64 forte e não pode ser versionado.

Exemplo Linux/macOS:

```bash
export DB_URL='jdbc:postgresql://localhost:5432/organizacao_recursos'
export DB_USERNAME='postgres'
export DB_PASSWORD='<SENHA_LOCAL_FORA_DO_GIT>'
export JWT_SECRET="$(openssl rand -base64 32)"
export JWT_EXPIRATION='PT2H'
./mvnw spring-boot:run
```

No PowerShell, gere o segredo em memória e informe a senha do banco sem gravá-la em arquivo:

```powershell
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
[Array]::Clear($bytes, 0, $bytes.Length)
$env:JWT_EXPIRATION = 'PT2H'
$securePassword = Read-Host 'Senha do PostgreSQL' -AsSecureString
$pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
try { $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer) }
finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer) }
.\mvnw.cmd spring-boot:run
```

## 6. Verificar o acesso

- Interface: `http://localhost:8080/`
- Health: `GET http://localhost:8080/api/health`
- Login: `POST http://localhost:8080/api/auth/login`
- Swagger UI: `http://localhost:8080/swagger-ui.html` (redireciona para `/swagger-ui/index.html`)
- OpenAPI: `http://localhost:8080/api-docs`

Use o e-mail e a senha definidos no bootstrap. O `JWT_SECRET` precisa ser fornecido novamente em cada nova sessão do backend.

## 7. Executar a validação do projeto

Para testes unitários e de integração, mantenha o Docker Desktop ativo:

Windows:

```powershell
.\mvnw.cmd -B verify
java scripts\ValidateRtm.java
```

Linux/macOS:

```bash
./mvnw -B verify
java scripts/ValidateRtm.java
```

`verify` executa os testes PostgreSQL via Testcontainers e os gates JaCoCo. O validador verifica links internos, IDs duplicados, referências de testes, evidências e relatórios Surefire; ele não substitui a avaliação funcional dos testes.

## Problemas comuns

- `JWT_SECRET` ausente: defina a variável antes de iniciar o backend.
- Falha de conexão: confirme host, porta, banco, usuário e senha do PostgreSQL.
- Banco não vazio: crie um banco local novo; não remova tabelas para forçar o bootstrap.
- Testcontainers inacessível: abra o Docker Desktop e confirme acesso ao daemon.
- Porta 8080 ocupada: encerre o processo que a utiliza antes de iniciar a aplicação.

## Referências do projeto

- [README principal](../README.md)
- [API de erros](api-erros.md)
- [Catálogo de API e fluxos](api-fluxos.md)
- [API de relatórios](api-relatorios.md)
- [Migrations](../src/main/resources/db/migration/)
- [Relatório de testes](testes/relatorio-testes.md)
