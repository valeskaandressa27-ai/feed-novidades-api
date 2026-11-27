# Backlog e status de entrega

## Escopo obrigatório — concluído

- [x] Repositório, branch de desenvolvimento, `.gitignore` e `.env.example`
- [x] Projeto Spring Boot com Java 21, Maven e PostgreSQL via Docker Compose
- [x] Migration Flyway para lojas, clientes, seguidores e novidades, com índice para o feed
- [x] Tratamento padronizado de erros HTTP (404, 409 e 422)
- [x] Cadastro de lojas: `POST /lojas`
- [x] Cadastro de clientes com e-mail único e normalizado: `POST /clientes`
- [x] Relacionamento cliente/loja com tratamento atômico de duplicidade e concorrência
- [x] Publicação de novidades com validação de texto e preço
- [x] Feed personalizado com fan-out on read, ordenação determinística e limite máximo de 50
- [x] Testes de integração com PostgreSQL via Testcontainers
- [x] Perfil de seed para dados de demonstração
- [x] Exemplos de requisição em `requests.http`
- [x] Documentação de arquitetura, execução, endpoints e decisões técnicas no README
- [x] Pipeline de integração contínua com GitHub Actions

## Verificações antes de uma nova apresentação

Estas verificações operacionais devem ser repetidas após alterações no código:

- [ ] Executar `mvn -B verify` com Docker disponível
- [ ] Confirmar a última execução do GitHub Actions como concluída e aprovada
- [ ] Reproduzir o passo a passo do README em um ambiente limpo

> A lista acima não indica funcionalidades obrigatórias ausentes. São verificações de qualidade a repetir antes de uma nova entrega ou alteração.

## Melhorias futuras — fora do escopo entregue

- [ ] Endpoint para deixar de seguir uma loja (`DELETE /clientes/{id}/seguindo/{lojaId}`)
- [ ] Paginação por cursor no feed
- [ ] Documentação interativa com OpenAPI/Swagger
- [ ] Testes unitários específicos para serviços, além dos testes de integração existentes
- [ ] Avaliar otimizações/cache somente após medições de desempenho
- [ ] Autenticação, autorização e rate limiting
- [ ] Maven Wrapper para padronizar a execução sem Maven instalado