# ADR 0001: Seguir loja com INSERT ... ON CONFLICT

## Status
Aceito

## Contexto
Dois pedidos simultâneos para o mesmo cliente seguir a mesma loja podiam
passar pela verificação prévia e gerar duplicidade ou erro de banco.

## Decisão
Usar `INSERT ... ON CONFLICT (cliente_id, loja_id) DO NOTHING`, apoiado
por uma restrição única. Se nenhuma linha for inserida, a API retorna 409.

## Consequências
- A unicidade é garantida pelo banco, sem janela de corrida.
- Não usar verificação prévia + INSERT.
