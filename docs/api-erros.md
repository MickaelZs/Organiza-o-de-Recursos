# Contrato de erros da API (RF13)

Respostas de erro são JSON com os campos:

```json
{
  "timestamp": "2026-11-05T00:00:00-03:00",
  "status": 409,
  "error": "Conflict",
  "message": "Horario indisponivel para o recurso.",
  "path": "/api/reservas"
}
```

## Status

- 400: body, parâmetro, data ou período inválido;
- 401: ausência de autenticação, token inválido ou expirado;
- 403: usuário autenticado sem o perfil ou ownership necessários;
- 404: entidade ou rota inexistente;
- 409: conflito de horário ou transição de estado inválida;
- 500: falha inesperada, com mensagem genérica e sem stack trace.

Handlers específicos preservam as mensagens de negócio existentes. `SecurityConfig` escreve o mesmo formato para 401 e 403. Senha/hash, segredo JWT, credenciais e stack trace não são retornados.
