# Backlog

## Obrigatório

- [x] Repositório, branch `feat/api-feed`, `.gitignore` e `.env.example`
- [x] Projeto Spring Boot (Java 21, Maven) e `docker-compose.yml` só com PostgreSQL
- [x] Migration Flyway: `loja`, `cliente`, `seguidor`, `novidade` + índice do feed
- [x] Formato único de erro com `@RestControllerAdvice` (404 / 409 / 422)
- [x] `POST /lojas`
- [x] `POST /clientes` (e-mail único, normalizado)
- [x] `POST /clientes/{id}/seguindo/{lojaId}` (404 e 409, inclusive em concorrência)
- [x] `POST /lojas/{id}/novidades` (texto 1–500, preço > 0)
- [x] `GET /clientes/{id}/feed?limite=20` (fan-out on read, limite padrão 20 / máx. 50)
- [x] Testes de integração com Testcontainers (feed, seguir, publicar, cliente, loja)
- [x] Seed (`-Dspring-boot.run.profiles=seed`)
- [x] Arquivo `requests.http` com exemplos
- [x] README com os itens exigidos

## Para fechar antes de entregar

- [ ] Rodar `mvn test` localmente (Docker ligado) e confirmar que passa
- [ ] Rodar o passo a passo do README em um clone limpo
- [ ] Conferir se o CI do GitHub Actions ficou verde
- [ ] Abrir o Pull Request `feat/api-feed` → `main` com a descrição preparada

## Diferenciais

- [x] CI simples com GitHub Actions
- [ ] Deixar de seguir (`DELETE /clientes/{id}/seguindo/{lojaId}`)
- [ ] Paginação por cursor no feed
- [ ] Documentação OpenAPI/Swagger
