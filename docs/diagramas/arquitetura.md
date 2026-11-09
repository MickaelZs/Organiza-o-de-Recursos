# Diagramas da solução

Os diagramas abaixo são Mermaid versionável e representam o código atualmente entregue.

## Contexto

```mermaid
flowchart LR
    U[Usuários<br/>SOLICITANTE / RESPONSAVEL / ADMIN]
    UI[Interface web responsiva<br/>HTML + CSS + JavaScript]
    API[API Spring Boot<br/>JWT + REST + Springdoc]
    DB[(PostgreSQL<br/>Flyway + JPA)]
    CI[GitHub Actions<br/>clean verify + JaCoCo]
    SONAR[SonarCloud<br/>condicional a secret/configuração]

    U --> UI
    UI --> API
    API --> DB
    CI --> API
    CI --> SONAR
```

## Componentes

```mermaid
flowchart TB
    Web[static/index.html<br/>styles.css<br/>app.js]
    Security[SecurityConfig<br/>JwtAuthenticationFilter<br/>JwtService]
    Controllers[Controllers REST]
    Services[Services de domínio]
    Repositories[Repositories Spring Data]
    Migrations[Flyway V1-V10]
    PostgreSQL[(PostgreSQL)]

    Web --> Controllers
    Controllers --> Security
    Controllers --> Services
    Services --> Repositories
    Repositories --> PostgreSQL
    Migrations --> PostgreSQL
    Security --> Services
```

## Sequência: reserva concorrente

```mermaid
sequenceDiagram
    participant A as Solicitação A
    participant B as Solicitação B
    participant S as ReservaService
    participant R as linha do recurso
    participant DB as PostgreSQL

    par solicitações simultâneas
        A->>S: POST /api/reservas
        B->>S: POST /api/reservas
    end
    S->>R: PESSIMISTIC_WRITE
    R->>DB: lock da sala/professor/material
    S->>DB: consulta conflitos e salva A
    DB-->>S: commit A libera o lock
    S->>R: B aguarda e adquire o lock
    S->>DB: consulta conflito de B
    DB-->>S: conflito sobreposto
    S-->>B: HTTP 409 + CONFLITO_EVITADO
```

A implementação concreta está em `ReservaService`, nos métodos de criação/atualização, e nos repositórios com `@Lock(PESSIMISTIC_WRITE)`. A evidência concorrente está em `ReservaConcorrenciaPostgresTest` e `ManutencaoReservaConcorrenciaPostgresTest`.
