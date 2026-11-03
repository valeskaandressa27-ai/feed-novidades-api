# Feed de Novidades API

API REST em que **clientes seguem lojas** e acompanham, em um **feed**, as novidades que essas lojas publicam (da mais recente para a mais antiga).

Stack: Java 21, Spring Boot 3.5, Spring Data JPA, PostgreSQL 16, Flyway, Docker Compose, JUnit 5 e Testcontainers.

## 1. Como rodar do zero

**Pré-requisitos:** Java 21, Maven 3.9+ e Docker com Docker Compose.

```bash
git clone <url-do-repositorio>
cd feed-novidades-api

docker compose up -d        # sobe o PostgreSQL
mvn spring-boot:run         # sobe a API em http://localhost:8080
```

O Flyway cria as tabelas automaticamente na primeira execução. O compose usa valores padrão, então **não é preciso criar `.env`**; copie `.env.example` para `.env` só se quiser mudar usuário, senha ou porta do banco. O Spring **não** lê o `.env`: se mudar esses valores, exporte também `DB_URL`, `DB_USER` e `DB_PASSWORD` (os nomes estão no `.env.example`).

**Dados de exemplo (opcional):** com o banco vazio, rode

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=seed
```

Cria 3 lojas, 3 clientes, os vínculos de seguidores e 7 novidades. Se o banco já tiver dados, o seed não faz nada.

Para parar o banco: `docker compose down` (acrescente `-v` para apagar os dados).

## 2. Como rodar os testes

```bash
mvn test
```

Os testes são de integração e usam **PostgreSQL real via Testcontainers**: precisam do Docker em execução, mas **não** dependem do `docker compose up` (o Testcontainers sobe um container próprio e descartável). Se o Docker não estiver disponível, os testes falham de propósito, em vez de trocar o banco por outro.

Cobertura: feed (só lojas seguidas, ordem, desempate, limite padrão/máximo/inválido), seguir (sucesso, 404, 409 e requisições simultâneas), publicar novidade (limites de texto, preço, loja inexistente, JSON malformado), cliente (e-mail duplicado, campos obrigatórios) e loja.

## 3. Exemplos de chamadas

Também em [`requests.http`](requests.http). Num banco vazio os ids começam em 1.

```bash
# Criar loja -> 201
curl -s -X POST localhost:8080/lojas -H 'Content-Type: application/json' \
  -d '{"nome":"Cafeteria Aurora"}'
# {"id":1,"nome":"Cafeteria Aurora"}

# Criar cliente -> 201 (e-mail é guardado em minúsculas)
curl -s -X POST localhost:8080/clientes -H 'Content-Type: application/json' \
  -d '{"nome":"Ana Souza","email":"ana@exemplo.com"}'
# {"id":1,"nome":"Ana Souza","email":"ana@exemplo.com"}

# Cliente 1 segue a loja 1 -> 201
curl -s -X POST localhost:8080/clientes/1/seguindo/1
# {"clienteId":1,"lojaId":1}

# Loja 1 publica novidade (preco é opcional) -> 201
curl -s -X POST localhost:8080/lojas/1/novidades -H 'Content-Type: application/json' \
  -d '{"texto":"Novo blend de café chegou!","preco":32.90}'
# {"id":1,"lojaId":1,"lojaNome":"Cafeteria Aurora","texto":"Novo blend de café chegou!","preco":32.90,"criadaEm":"2026-11-03T15:04:05.123456Z"}

# Feed do cliente 1 -> 200 (limite padrão 20, máximo 50)
curl -s 'localhost:8080/clientes/1/feed?limite=10'
# [{"id":1,"lojaId":1,"lojaNome":"Cafeteria Aurora", ... }]
```

### Formato de erro (igual em toda a API)

```json
{
  "status": 422,
  "erro": "VALIDACAO",
  "mensagem": "Dados inválidos",
  "detalhes": ["texto: deve ter no máximo 500 caracteres"]
}
```

| Situação | Status | `erro` |
|---|---|---|
| Campo ausente/inválido, JSON malformado, id ou `limite` não numérico, `limite` < 1 | 422 | `VALIDACAO` |
| Cliente ou loja inexistente (e rota inexistente) | 404 | `NAO_ENCONTRADO` |
| E-mail já cadastrado; cliente já segue a loja | 409 | `CONFLITO` |
| Método não permitido, tipo de mídia não suportado | 405 / 415 | `REQUISICAO_INVALIDA` |
| Falha inesperada (sem stack trace na resposta) | 500 | `ERRO_INTERNO` |

## 4. Estrutura de pastas

```
src/main/java/com/feednovidades/
├── FeedNovidadesApplication.java
├── loja/        entidade, repositório, serviço, controller e DTOs de loja
├── cliente/     idem para cliente
├── seguidor/    entidade com chave composta + serviço e endpoint de seguir
├── novidade/    entidade, DTOs, serviço de publicação e a consulta do feed
├── feed/        endpoint e regra do feed (limite padrão/máximo)
├── erro/        formato único de erro, exceções e @RestControllerAdvice
└── seed/        carga de dados de exemplo (só com o perfil "seed")
src/main/resources/
├── application.yml
└── db/migration/V1__criar_tabelas.sql
src/test/java/com/feednovidades/   testes de integração (Testcontainers)
docker-compose.yml  .env.example  requests.http  BACKLOG.md
.github/workflows/ci.yml
```

Organização **por funcionalidade** (não por camada): cada pasta tem tudo o que aquele assunto precisa, com controller → service → repository dentro dela.

## 5. Decisões técnicas

**Feed com fan-out on read.** O enunciado diz que o cliente abre o feed várias vezes por dia, a loja publica poucas vezes e lojas grandes passam de 100 mil seguidores. No *fan-out on write*, cada publicação de uma loja grande copiaria a novidade para 100 mil feeds, e isso para uma escrita rara. No *fan-out on read* a publicação é um único INSERT e o feed é montado na leitura, por um JOIN com `ORDER BY ... LIMIT` feito no banco (nada é carregado em memória para filtrar):

```sql
select n.* from novidade n
join seguidor s on s.loja_id = n.loja_id
where s.cliente_id = :clienteId
order by n.criada_em desc, n.id desc
limit :limite
```

O custo da leitura cresce com quantas lojas o cliente segue, não com quantos seguidores a loja tem, que é o lado que o enunciado diz ser grande.

**Índice `(loja_id, criada_em DESC, id DESC)`.** Entrega as novidades de cada loja já ordenadas. Em uma medição pontual com dados sintéticos (2.000 lojas, 200 mil novidades, cliente seguindo 5 lojas, PostgreSQL 16 local) o plano usou esse índice e o feed levou cerca de 0,3 ms. O `id` no índice acompanha o desempate da ordenação.

**Desempate por `id`.** Duas novidades podem ter o mesmo `criada_em`; sem desempate a ordem poderia variar entre chamadas.

**`seguidor` com PK composta `(cliente_id, loja_id)`.** O próprio banco impede seguir duas vezes. O serviço também checa antes (para dar mensagem clara), e a violação de PK por requisições simultâneas é convertida em 409.

**E-mail normalizado em minúsculas** antes de salvar, para `Ana@x.com` e `ana@x.com` contarem como o mesmo e-mail. A constraint `UNIQUE` é a garantia final.

**Validação em duas camadas.** Bean Validation nos DTOs (mensagens claras) + `CHECK` no banco (`preco > 0`, texto entre 1 e 500 caracteres): se alguém inserir dados por outro caminho, o banco ainda protege.

**422 para entrada inválida** (e não 400), incluindo JSON malformado e parâmetros não numéricos, para o cliente da API ter uma regra só: 422 = a requisição não pôde ser processada como veio.

**`limite`:** ausente = 20; maior que 50 = **limitado a 50** (sem erro); menor que 1 ou não numérico = 422. O enunciado não define o caso "acima de 50"; limitar é a opção mais tolerante para quem consome.

**Flyway manda no schema; o Hibernate só valida** (`ddl-auto: validate`). Se entidade e migration divergirem, a aplicação não sobe.

**DTOs (records)** nas entradas e saídas: as entidades JPA não vazam para a API.

**Datas.** `TIMESTAMPTZ`; `criada_em` é preenchido pela aplicação em UTC (microssegundos, a precisão do PostgreSQL), com `DEFAULT now()` no banco como rede de segurança.

**Testcontainers em vez de H2**, porque o que importa testar (constraints, tipos, ordenação, concorrência) é comportamento do PostgreSQL. Um container compartilhado por toda a execução; o banco é limpo antes de cada teste.

**Seed por perfil** (`seed`): nunca roda por acidente nem durante os testes.

**Sem autenticação**, conforme o enunciado: os ids vêm na rota.

## 6. O que ficou de fora e o que eu faria com mais tempo

- **Paginação por cursor** no feed (chave `(criada_em, id)`): hoje só há `limite`, então não dá para ver além das N mais recentes.
- **Deixar de seguir** (`DELETE /clientes/{id}/seguindo/{lojaId}`), documentação **OpenAPI** e testes unitários dos serviços.
- **Clientes que seguem muitas lojas com muito histórico:** a consulta lê as novidades de todas as lojas seguidas antes de ordenar. Uma evolução é buscar as N mais recentes *por loja* (`LATERAL`) e só então juntar, ou limitar por janela de tempo.
- **Cache** do feed (ex.: Redis, invalidado na publicação) se a leitura virar gargalo.
- **Autenticação/autorização** e limite de taxa.
- Um **`mvnw`** (Maven Wrapper) para dispensar a instalação do Maven.
