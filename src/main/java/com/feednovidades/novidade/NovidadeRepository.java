package com.feednovidades.novidade;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NovidadeRepository extends JpaRepository<Novidade, Long> {

    /**
     * Feed por fan-out on read: filtra, ordena e limita direto no banco.
     * O desempate por id garante ordem determinística quando criada_em empata.
     */
    @Query("""
            select new com.feednovidades.novidade.NovidadeResponse(
                n.id, l.id, l.nome, n.texto, n.preco, n.criadaEm)
            from Novidade n
            join n.loja l
            join Seguidor s on s.id.lojaId = l.id
            where s.id.clienteId = :clienteId
            order by n.criadaEm desc, n.id desc
            """)
    List<NovidadeResponse> buscarFeed(@Param("clienteId") Long clienteId, Pageable pageable);
}
