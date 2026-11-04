package com.feednovidades.seguidor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeguidorRepository extends JpaRepository<Seguidor, SeguidorId> {

    /**
     * Cria o vínculo cliente-loja de forma atômica: o PostgreSQL decide, em um único
     * comando, se o par já existe. Retorna 1 se inseriu e 0 se o par já existia.
     *
     * Não usamos save()/saveAndFlush(): como a chave é atribuída (não gerada), o Spring Data
     * faz merge(), que primeiro consulta a linha. Sob concorrência, o merge podia encontrar o
     * vínculo recém-criado por outra requisição, não inserir nada e ainda assim responder 201.
     */
    @Modifying
    @Query(value = """
            INSERT INTO seguidor (cliente_id, loja_id)
            VALUES (:clienteId, :lojaId)
            ON CONFLICT (cliente_id, loja_id) DO NOTHING
            """, nativeQuery = true)
    int inserirSeNaoExistir(@Param("clienteId") Long clienteId, @Param("lojaId") Long lojaId);
}
