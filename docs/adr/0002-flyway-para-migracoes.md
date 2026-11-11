# ADR 0002: Flyway para migrações do banco

## Status
Aceito

## Contexto
A API usa PostgreSQL e precisa que o esquema do banco seja criado do
mesmo jeito em qualquer ambiente: máquina local, testes e CI.

## Decisão
Usar o Flyway para versionar o esquema com migrações SQL, executadas
automaticamente na subida da aplicação. O esquema não é gerado pelo
Hibernate.

## Consequências
- O histórico de mudanças do banco fica versionado junto com o código.
- Ambientes novos sobem com o esquema correto, sem passos manuais.
- Mudanças no esquema exigem uma nova migração, nunca a edição de uma já aplicada.
