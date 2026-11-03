package com.feednovidades.novidade;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Representação de uma novidade, usada tanto na publicação quanto no feed.
 * O construtor canônico é usado pela consulta JPQL do feed (select new ...).
 */
public record NovidadeResponse(
        Long id,
        Long lojaId,
        String lojaNome,
        String texto,
        BigDecimal preco,
        Instant criadaEm) {

    static NovidadeResponse de(Novidade n) {
        return new NovidadeResponse(
                n.getId(), n.getLoja().getId(), n.getLoja().getNome(),
                n.getTexto(), n.getPreco(), n.getCriadaEm());
    }
}
