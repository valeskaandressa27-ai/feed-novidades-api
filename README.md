# Feed de Novidades API

[![CI](https://github.com/valeskaandressa27-ai/feed-novidades-api/actions/workflows/ci.yml/badge.svg)](https://github.com/valeskaandressa27-ai/feed-novidades-api/actions/workflows/ci.yml)

API REST desenvolvida com **Java 21 e Spring Boot** para conectar clientes e lojas: clientes seguem lojas e recebem, em um feed personalizado, as novidades publicadas pelas lojas que acompanham.

O projeto demonstra implementação de APIs REST, persistência relacional, integridade de dados, tratamento padronizado de erros, testes de integração com PostgreSQL e automação de CI.


## Acesso ao projeto

- **Código-fonte:** [valeskaandressa27-ai/feed-novidades-api](https://github.com/valeskaandressa27-ai/feed-novidades-api)
- **Execução local:** siga as instruções em [Como executar localmente](#como-executar-localmente).

> **Demonstração online:** este repositório não possui, no momento, uma URL pública de uma instância da API. Para testar os endpoints, execute a aplicação localmente conforme as instruções abaixo.


## Visão geral

- Cadastro de lojas e clientes.
- Cadastro de clientes com e-mail único e normalizado.
- Relacionamento entre clientes e lojas seguidas.
- Publicação de novidades com texto e preço opcional.
- Feed personalizado, ordenado das publicações mais recentes para as mais antigas.
- Limite configurável para o feed, com máximo de 50 registros.
- Tratamento consistente de erros HTTP e validações.
- Testes de integração com PostgreSQL real por meio do Testcontainers.

## Tecnologias

| Área | Tecnologias |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.5.16 |
| Persistência | Spring Data JPA / Hibernate |
| Banco de dados | PostgreSQL 16 |
| Migrações | Flyway |
| Validação | Bean Validation |
| Testes | JUnit 5, Spring Boot Test e Testcontainers |
| Build | Maven |
| Ambiente local | Docker Compose |
| Integração contínua | GitHub Actions |

## Arquitetura e decisões técnicas

A aplicação está organizada **por funcionalidade/domínio**, separando os módulos de loja, cliente, seguidor, novidade, feed e tratamento de erros. Dentro de cada módulo, as responsabilidades de controller, service, repository e DTO ficam próximas.

### Estratégia do feed: fan-out on read

A publicação de uma novidade grava um único registro. O feed é montado quando o cliente consulta a API, por meio de um JOIN entre novidades e seguidores, com ordenação e limite executados no PostgreSQL.

Essa estratégia foi escolhida porque o cenário considera publicações relativamente pouco frequentes e lojas que podem ter muitos seguidores. Assim, evita-se replicar cada publicação para milhares de feeds no momento da escrita.

Consulta conceitual utilizada:

```sql
select n.*
from novidade n
join seguidor s on s.loja_id = n.loja_id
where s.cliente_id = :clienteId
order by n.criada_em desc, n.id desc
limit :limite
```

### Integridade e concorrência

- A tabela `seguidor` utiliza chave primária composta por `(cliente_id, loja_id)`, impedindo vínculos duplicados.
- A operação de seguir utiliza `INSERT ... ON CONFLICT DO NOTHING`. A decisão de inserir ou ignorar o vínculo é atômica no PostgreSQL; quando nenhuma linha é inserida, a API responde com conflito (409).
- A estratégia evita uma condição de corrida que poderia ocorrer ao usar `save()` do JPA com chave atribuída e operações concorrentes.
- O e-mail é normalizado para minúsculas antes da persistência, e uma constraint `UNIQUE` garante a unicidade no banco.
- As validações são aplicadas na camada da API e reforçadas por constraints no banco, incluindo texto entre 1 e 500 caracteres e preço maior que zero.

### Consulta e ordenação

- O feed usa `ORDER BY criada_em DESC, id DESC` para manter uma ordenação determinística quando duas novidades possuem o mesmo timestamp.
- O índice `(loja_id, criada_em DESC, id DESC)` auxilia a consulta das novidades por loja já na ordem necessária.
- O feed é consultado no banco, sem carregar todas as novidades para filtrar em memória.

### Persistência e datas

- O Flyway é responsável pela evolução do schema.
- O Hibernate utiliza `ddl-auto: validate`, verificando se o mapeamento das entidades corresponde ao schema criado pelas migrations.
- As entradas e saídas da API usam DTOs (records), evitando expor diretamente as entidades JPA.
- Os timestamps usam `TIMESTAMPTZ`; a aplicação preenche `criada_em` em UTC, com `DEFAULT now()` no banco como proteção adicional.
- Os testes usam PostgreSQL via Testcontainers em vez de H2 para validar constraints, tipos, ordenação e concorrência no mesmo banco utilizado pela aplicação.

As decisões arquiteturais também estão documentadas em [docs/adr](docs/adr).

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| POST | `/lojas` | Cadastra uma loja. |
| POST | `/clientes` | Cadastra um cliente; o e-mail deve ser único. |
| POST | `/clientes/{id}/seguindo/{lojaId}` | Faz um cliente seguir uma loja. |
| POST | `/lojas/{id}/novidades` | Publica uma novidade para uma loja. |
| GET | `/clientes/{id}/feed?limite=20` | Retorna o feed personalizado do cliente. |

**Regras do feed:** o limite padrão é 20; valores acima de 50 são limitados a 50; valores menores que 1 ou não numéricos retornam 422.

## Como executar localmente

### Pré-requisitos

- Java 21
- Maven 3.9+
- Docker com Docker Compose

### 1. Clonar o repositório

```bash
git clone https://github.com/valeskaandressa27-ai/feed-novidades-api.git
cd feed-novidades-api
```

### 2. Iniciar o PostgreSQL

```bash
docker compose up -d
```

O Docker Compose utiliza valores padrão; não é necessário criar um arquivo `.env`. O arquivo `.env.example` pode ser copiado para personalizar usuário, senha ou porta. O Spring não carrega o `.env` automaticamente: ao alterar essas configurações, exporte também `DB_URL`, `DB_USER` e `DB_PASSWORD`, conforme os nomes definidos no exemplo.

### 3. Iniciar a API

```bash
mvn spring-boot:run
```

A API ficará disponível em:

`http://localhost:8080`

O Flyway cria as tabelas automaticamente na primeira execução.

### Dados de exemplo (opcional)

Com o banco vazio, execute:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=seed
```

O perfil `seed` cria 3 lojas, 3 clientes, os vínculos de seguidores e 7 novidades. Se o banco já tiver dados, a carga não altera o conteúdo existente.

Para parar o banco:

```bash
docker compose down
```

Para remover também os dados persistidos, use `docker compose down -v`.

## Exemplos de requisições

Os exemplos também estão disponíveis em [requests.http](requests.http). Em um banco vazio, os IDs começam em 1.

### Criar uma loja

```bash
curl -s -X POST localhost:8080/lojas \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Cafeteria Aurora"}'
```

Resposta esperada: `201 Created`.

```json
{"id":1,"nome":"Cafeteria Aurora"}
```

### Criar um cliente

```bash
curl -s -X POST localhost:8080/clientes \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Ana Souza","email":"ana@exemplo.com"}'
```

Resposta esperada: `201 Created`. O e-mail é armazenado em minúsculas.

```json
{"id":1,"nome":"Ana Souza","email":"ana@exemplo.com"}
```

### Seguir uma loja

```bash
curl -s -X POST localhost:8080/clientes/1/seguindo/1
```

Resposta esperada: `201 Created`.

```json
{"clienteId":1,"lojaId":1}
```

### Publicar uma novidade

O campo `preco` é opcional.

```bash
curl -s -X POST localhost:8080/lojas/1/novidades \
  -H 'Content-Type: application/json' \
  -d '{"texto":"Novo blend de café chegou!","preco":32.90}'
```

Resposta esperada: `201 Created`.

```json
{
  "id": 1,
  "lojaId": 1,
  "lojaNome": "Cafeteria Aurora",
  "texto": "Novo blend de café chegou!",
  "preco": 32.90,
  "criadaEm": "2026-11-03T15:04:05.123456Z"
}
```

### Consultar o feed

```bash
curl -s 'localhost:8080/clientes/1/feed?limite=10'
```

Resposta esperada: `200 OK`, com as novidades das lojas seguidas pelo cliente, da mais recente para a mais antiga.

## Tratamento de erros

A API utiliza um formato padronizado de resposta:

```json
{
  "status": 422,
  "erro": "VALIDACAO",
  "mensagem": "Dados inválidos",
  "detalhes": [
    "texto: deve ter no máximo 500 caracteres"
  ]
}
```

| Situação | HTTP | Código |
|---|---:|---|
| Campo ausente/inválido, JSON malformado ou parâmetro inválido | 422 | `VALIDACAO` |
| Cliente ou loja inexistente, ou rota inexistente | 404 | `NAO_ENCONTRADO` |
| E-mail já cadastrado ou cliente já segue a loja | 409 | `CONFLITO` |
| Método não permitido ou tipo de mídia não suportado | 405 / 415 | `REQUISICAO_INVALIDA` |
| Falha inesperada, sem stack trace na resposta | 500 | `ERRO_INTERNO` |

## Testes e integração contínua

Execute os testes com:

```bash
mvn test
```

Os testes de integração utilizam **PostgreSQL real via Testcontainers**. É necessário ter Docker em execução, mas não é preciso iniciar o serviço do `docker-compose`: o Testcontainers sobe um container próprio e descartável. Se o Docker não estiver disponível, os testes falham intencionalmente, sem substituir o PostgreSQL por outro banco.

A suíte cobre:

- Feed: lojas seguidas, ordenação, desempate, limite padrão, máximo e valores inválidos.
- Seguir loja: sucesso, recursos inexistentes, vínculo duplicado e requisições simultâneas.
- Publicação: limites de texto, preço, loja inexistente e JSON malformado.
- Clientes: e-mail duplicado e campos obrigatórios.
- Lojas: operações de cadastro.

O GitHub Actions executa `mvn -B verify` em pushes e pull requests, usando Java 21 e Docker no runner.

**Diagnóstico do Testcontainers:** se aparecer `Could not find a valid Docker environment`, confirme que o Docker está ativo. O projeto fixa Testcontainers 1.21.4 para compatibilidade com versões recentes do Docker Engine.

## Estrutura do projeto

```text
src/main/java/com/feednovidades/
├── FeedNovidadesApplication.java
├── loja/        entidade, repositório, serviço, controller e DTOs
├── cliente/     entidade, repositório, serviço, controller e DTOs
├── seguidor/    entidade com chave composta, serviço e endpoint
├── novidade/    entidade, DTOs, serviço de publicação e consulta do feed
├── feed/        endpoint e regra de limite do feed
├── erro/        formato de erro, exceções e @RestControllerAdvice
└── seed/        carga de dados de exemplo (perfil seed)

src/main/resources/
├── application.yml
└── db/migration/V1__criar_tabelas.sql

src/test/java/com/feednovidades/   testes de integração
docker-compose.yml
.env.example
requests.http
BACKLOG.md
.github/workflows/ci.yml
```

## Próximas evoluções

Os itens abaixo são **planejados e não fazem parte da implementação atual**:

- Paginação por cursor no feed, usando a chave `(criada_em, id)`.
- Endpoint para deixar de seguir uma loja: `DELETE /clientes/{id}/seguindo/{lojaId}`.
- Documentação interativa com OpenAPI/Swagger.
- Testes unitários específicos para os serviços.
- Otimização para clientes que seguem muitas lojas e possuem grande histórico de novidades, avaliando consulta com `LATERAL` ou janela temporal.
- Cache do feed (por exemplo, Redis), caso medições indiquem gargalo de leitura.
- Autenticação, autorização e rate limiting.
- Maven Wrapper para padronizar a execução sem instalação prévia do Maven.
- Atualização futura para uma linha do Spring Boot com suporte open source ativo. A versão 3.5.16 utilizada neste projeto foi a última versão open source da linha 3.5.x.

## Status do projeto

Projeto desenvolvido para demonstrar conhecimentos em desenvolvimento backend e construção de APIs REST com Java, Spring Boot e PostgreSQL, incluindo modelagem relacional, concorrência, testes de integração e CI.

---

Desenvolvido por [Valeska Oliveira](https://github.com/valeskaandressa27-ai).
