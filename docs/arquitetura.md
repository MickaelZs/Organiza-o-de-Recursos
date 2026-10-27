# Arquitetura

Arquitetura monolítica em camadas:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
```

Integrações externas deverão passar por uma abstração/client próprio. A escolha privilegia simplicidade, separação de responsabilidades, testabilidade, manutenção e facilidade de compreensão na avaliação oral. Não há arquitetura distribuída nesta solução.
