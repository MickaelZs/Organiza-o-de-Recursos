# Análise ATAM e árvore de utilidade

## Escopo e limites

Esta é uma análise arquitetural preliminar para apoiar a revisão da Etapa 6.
Ela usa a arquitetura documentada em [arquitetura.md](arquitetura.md), os
requisitos da [RTM](rtm.md), os testes disponíveis e o comportamento observado
no código. Não representa uma sessão ATAM facilitada com o grupo: cenários,
prioridades e riscos precisam ser confirmados pelos integrantes.

## Árvore de utilidade

```text
Qualidade do sistema
├── Segurança
│   ├── Autenticação e autorização por perfil (alta)
│   ├── Proteção de segredos fora do repositório (alta)
│   └── Bootstrap do primeiro ADMIN somente em banco local vazio (alta)
├── Integridade
│   ├── Evitar reserva duplicada do mesmo recurso (alta)
│   ├── Evitar conflito entre reserva e manutenção (alta)
│   └── Preservar trilha de auditoria em transação independente (média)
├── Desempenho
│   ├── Responder às consultas e fluxos usuais com latência aceitável (média)
│   └── Sustentar concorrência de solicitações sem violar integridade (alta)
├── Modificabilidade
│   ├── Separar controller, serviço e persistência (média)
│   └── Manter contratos API e requisitos rastreáveis (média)
└── Operabilidade
    ├── Instalação reprodutível em clone limpo (alta)
    ├── Diagnóstico por logs/health checks e erros seguros (média)
    └── Evidência de carga reproduzível (média)
```

As prioridades são uma proposta técnica desta auditoria, não uma votação do
grupo. Não foram definidos alvos numéricos de latência porque não há execução
JMeter atual com dados brutos que os sustentem.

## Cenários arquiteturais prioritários

| ID | Estímulo e ambiente | Resposta esperada | Evidência atual | Situação |
|---|---|---|---|---|
| ATAM-01 | Duas requisições concorrentes tentam reservar o mesmo recurso e período | No máximo uma reserva conflitante é confirmada; estado persistido permanece consistente | Testes PostgreSQL concorrentes de reserva e manutenção no `clean verify` | Coberto para os cenários testados |
| ATAM-02 | Requisição sem autenticação, com perfil incorreto ou acesso a recurso alheio | Negar acesso com 401/403 sem expor dados protegidos | Testes de autorização e ownership | Coberto pela suíte; inspeção manual pendente |
| ATAM-03 | Operador inicia bootstrap com URL remota, banco não vazio ou duas inicializações simultâneas | Recusar alvo inseguro; preservar dados existentes; criar no máximo um ADMIN inicial | Testes do serviço/comando e lock advisory PostgreSQL | Coberto automaticamente; fluxo manual pendente |
| ATAM-04 | Equipe tenta validar capacidade sob carga com carga e banco descartáveis | Produzir métricas brutas rastreáveis ao commit e à configuração usada | Planos JMX existentes; resultado atual não executado | Pendente |
| ATAM-05 | Desenvolvedor clona o commit avaliado em ambiente compatível com Docker | `clean verify`, Testcontainers, JaCoCo e validador RTM passam | Clone temporário limpo, Java 21, Docker e PostgreSQL Testcontainers | Reproduzido para o commit de código citado no relatório |

## Trade-offs e riscos

- O monólito em camadas reduz custo de operação e facilita a entrega acadêmica,
  mas concentra implantação e escala em uma única aplicação.
- O bloqueio pessimista e as constraints no PostgreSQL protegem a integridade
  concorrente, ao custo de contenção em períodos de alta disputa. Os testes
  confirmam os casos concorrentes cobertos, mas não medem throughput.
- JWT facilita autenticação stateless; gestão e rotação do segredo dependem da
  configuração externa e da operação do ambiente.
- O bootstrap conservador reduz risco de inicializar no banco errado, mas exige
  PostgreSQL local e interação manual para validar a primeira conta.
- JaCoCo, CI e SonarCloud dão sinais automatizados de qualidade, mas não
  substituem inspeção visual, teste exploratório nem medição de carga.

## Pontos de atenção e próximos passos

1. Executar o plano JMeter em ambiente descartável e guardar JTL/relatório,
   configuração, SHA e versão do JMeter.
2. Fazer inspeção visual desktop/mobile e percorrer login e fluxos por perfil.
3. Confirmar em reunião do grupo prioridades, cenários de qualidade e riscos
   aceitos; registrar nomes e decisão em `decisao-qualidade.md`.
4. Revisar os diagramas e este documento com os integrantes antes de associar
   a análise a uma release candidate.
