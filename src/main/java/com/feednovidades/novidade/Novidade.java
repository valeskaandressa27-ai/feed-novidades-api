package com.feednovidades.novidade;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.feednovidades.loja.Loja;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "novidade")
public class Novidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loja_id", nullable = false)
    private Loja loja;

    @Column(nullable = false, length = 500)
    private String texto;

    @Column(precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(name = "criada_em", nullable = false)
    private Instant criadaEm;

    protected Novidade() {
    }

    public Novidade(Loja loja, String texto, BigDecimal preco) {
        // Truncado em microssegundos: é a precisão do PostgreSQL.
        this(loja, texto, preco, Instant.now().truncatedTo(ChronoUnit.MICROS));
    }

    public Novidade(Loja loja, String texto, BigDecimal preco, Instant criadaEm) {
        this.loja = loja;
        this.texto = texto;
        this.preco = preco;
        this.criadaEm = criadaEm;
    }

    public Long getId() {
        return id;
    }

    public Loja getLoja() {
        return loja;
    }

    public String getTexto() {
        return texto;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public Instant getCriadaEm() {
        return criadaEm;
    }
}
