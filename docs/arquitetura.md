# Arquitetura

Arquitetura monolítica em camadas, com interface web estática responsiva servida pelo mesmo Spring Boot:

```text
Interface web (HTML/CSS/JavaScript) / cliente REST
    ↓
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
```

Integrações externas deverão passar por uma abstração/client próprio. A escolha privilegia simplicidade, separação de responsabilidades, testabilidade, manutenção e facilidade de compreensão na avaliação oral. Não há arquitetura distribuída nesta solução. Os diagramas de contexto, componentes e sequência estão em [docs/diagramas/arquitetura.md](diagramas/arquitetura.md).
