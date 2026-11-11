# ADR 0004: Docker Compose para o ambiente local

## Status
Aceito

## Contexto
A API depende de um PostgreSQL. Quem for rodar o projeto pela primeira
vez não deveria precisar instalar e configurar o banco na própria máquina.

## Decisão
Usar o Docker Compose para subir o PostgreSQL local, com a configuração
no arquivo docker-compose.yml e as variáveis de exemplo no .env.example.

## Consequências
- O projeto roda do zero com poucos comandos, como descrito no README.
- A versão do PostgreSQL fica fixa e igual para todos.
- É preciso ter o Docker e o Docker Compose instalados.
