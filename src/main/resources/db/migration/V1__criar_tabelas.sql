CREATE TABLE loja (
    id   BIGSERIAL PRIMARY KEY,
    nome VARCHAR(120) NOT NULL
);

CREATE TABLE cliente (
    id    BIGSERIAL PRIMARY KEY,
    nome  VARCHAR(120) NOT NULL,
    email VARCHAR(254) NOT NULL,
    CONSTRAINT uk_cliente_email UNIQUE (email)
);

-- A PK composta impede que um cliente siga a mesma loja duas vezes.
CREATE TABLE seguidor (
    cliente_id BIGINT NOT NULL REFERENCES cliente (id),
    loja_id    BIGINT NOT NULL REFERENCES loja (id),
    PRIMARY KEY (cliente_id, loja_id)
);

CREATE TABLE novidade (
    id        BIGSERIAL PRIMARY KEY,
    loja_id   BIGINT         NOT NULL REFERENCES loja (id),
    texto     VARCHAR(500)   NOT NULL,
    preco     NUMERIC(10, 2),
    criada_em TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT ck_novidade_texto CHECK (char_length(btrim(texto)) BETWEEN 1 AND 500),
    CONSTRAINT ck_novidade_preco CHECK (preco IS NULL OR preco > 0)
);

-- Sustenta o feed: novidades de uma loja já na ordem usada na consulta
-- (o id no fim é o desempate de ordenação).
CREATE INDEX idx_novidade_loja_criada_em ON novidade (loja_id, criada_em DESC, id DESC);
