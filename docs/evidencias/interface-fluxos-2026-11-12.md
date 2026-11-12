# Validação funcional em Chromium — 12/11/2026

Base `412c4a1`, aplicação local com PostgreSQL 16 descartável, perfis e recursos sintéticos. Ações abaixo foram executadas nos controles da interface e conferidas pelas respostas HTTP observadas; nenhuma credencial ou token é incluído. Login e logout dos três perfis, cadastros, filtros e responsividade foram registrados anteriormente no [índice](README.md).

| Fluxo | Resultado observado |
|---|---|
| Criar reserva restrita e aprovar | PASS: criação 201 em SOLICITADA; aprovação 200. |
| Não comparecimento | PASS: ação 200; banco confirmou NAO_COMPARECEU e evento de auditoria. |
| Rejeitar solicitação | PASS: ação 200; banco confirmou REJEITADA e evento. |
| Retirada e devolução | PASS: material aprovado, ações 200; banco confirmou CONCLUIDA e eventos RETIRADA/DEVOLVIDA. |
| Alterar e cancelar reserva | PASS: PUT 200 e DELETE 204; banco confirmou CANCELADA e eventos ALTERADA/CANCELADA. |
| Manutenção | PASS: criação 201; reserva bloqueada com 409 e mensagem; cancelamento 204; reserva após liberação 201. |
| Auditoria | PASS: lista exibida; banco confirmou eventos de aprovação, rejeição, alteração, cancelamento, retirada, devolução e não comparecimento. |
| Notificações | PASS: lista apresentada e marcação de leitura respondeu 200 após ações de reserva. |
| Relatórios | PASS: formulário exibiu utilização, carga horária e conflitos evitados; a correção dos totais possui testes PostgreSQL na suíte automatizada. |
| Autorização | PASS no escopo ensaiado: três perfis exibidos; cadastro apresentado somente ao ADMIN; SOLICITANTE sem abas administrativas e auditoria respondeu 403. |
| Formulário inválido | PASS: término anterior ao início retornou 400 com mensagem visível; conflito de manutenção apresentou mensagem. |

Consulta no banco após os ensaios encontrou zero pares de reservas ativas sobrepostas de sala, professor e material. O ensaio visual não representa dispositivos físicos nem valida todos os dados e combinações possíveis. A edição de cadastros de recursos não é requisito funcional explicitado no escopo fornecido; a alteração de reservas foi exercitada acima.
