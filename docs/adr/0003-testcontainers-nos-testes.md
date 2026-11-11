# ADR 0003: Testcontainers nos testes de integração

## Status
Aceito

## Contexto
Os testes de integração precisam rodar contra um PostgreSQL real, para
validar migrações, restrições únicas e o comportamento do ON CONFLICT.
Um banco em memória se comporta de forma diferente do PostgreSQL.

## Decisão
Usar o Testcontainers para subir um PostgreSQL descartável durante os
testes, tanto na máquina local quanto no GitHub Actions.

## Consequências
- Os testes rodam contra o mesmo tipo de banco usado em produção.
- Não há banco compartilhado nem estado sobrando entre execuções.
- É preciso ter o Docker disponível para rodar os testes.
