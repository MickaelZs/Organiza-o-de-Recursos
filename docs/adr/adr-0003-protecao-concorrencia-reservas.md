# ADR-0003: proteção contra reservas concorrentes

- Status: aceita
- Data: 2026-11-03

## Contexto

A verificação RF-05 consulta reservas sobrepostas antes de inserir. Sem coordenação entre transações, duas instâncias podem consultar ao mesmo tempo, ambas não encontrar conflito e confirmar reservas incompatíveis.

## Alternativas consideradas

- `synchronized` ou lock em memória: não coordena múltiplas instâncias e foi descartado.
- Lock otimista em `Reserva`: as duas operações ainda não têm uma linha de reserva comum para versionar; não impede a corrida de inserção.
- Constraint única: não expressa diretamente intervalos sobrepostos nas referências polimórficas atuais. Uma constraint de exclusão exigiria mudança do modelo e/ou migrations fora desta etapa.
- Isolamento `SERIALIZABLE`: pode abortar transações concorrentes e exigiria política de retry; amplia o impacto de serialização além do recurso disputado.
- Lock pessimista na linha do recurso: usa a linha existente de Sala, Professor ou Material como ponto de coordenação.

## Decisão

Dentro da transação de criação ou atualização, a aplicação adquire lock `PESSIMISTIC_WRITE` no recurso escolhido antes de consultar conflitos e persistir. O PostgreSQL mantém o lock até o commit ou rollback. Outra transação que tenta reservar o mesmo recurso aguarda; depois da liberação, a consulta de conflitos em `READ COMMITTED` observa a reserva já confirmada e rejeita a segunda operação conforme RF-05.

O lock é por linha e por tabela: operações sobre recursos diferentes podem avançar em paralelo. A estratégia vale entre instâncias que usem o mesmo PostgreSQL e passem pelo fluxo transacional da aplicação.

## Limitações

- Todas as escritas de reserva que podem ocupar um horário precisam bloquear a mesma linha do recurso antes de verificar conflitos. Escritas diretas no banco ou fluxos que ignorem essa ordem não ficam protegidos por esta estratégia.
- Uma transação lenta mantém o lock por mais tempo e pode aumentar a espera de outras solicitações para o mesmo recurso.
- Não há mudança de schema ou migration; os recursos existentes são as linhas bloqueadas.
- A estratégia não implementa aprovação, manutenção nem política de retry para timeout de lock.
