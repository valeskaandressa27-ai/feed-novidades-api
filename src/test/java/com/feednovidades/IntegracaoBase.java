package com.feednovidades;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

import com.feednovidades.cliente.Cliente;
import com.feednovidades.cliente.ClienteRepository;
import com.feednovidades.loja.Loja;
import com.feednovidades.loja.LojaRepository;
import com.feednovidades.novidade.Novidade;
import com.feednovidades.novidade.NovidadeRepository;
import com.feednovidades.seguidor.Seguidor;
import com.feednovidades.seguidor.SeguidorRepository;

/**
 * Base dos testes de integração: PostgreSQL real (Testcontainers), um único
 * container para toda a execução e banco limpo antes de cada teste.
 * Requer Docker em execução.
 */
@SpringBootTest
@AutoConfigureMockMvc
abstract class IntegracaoBase {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void propriedadesDoBanco(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    protected MockMvc mvc;
    @Autowired
    protected JdbcTemplate jdbc;
    @Autowired
    private LojaRepository lojaRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private SeguidorRepository seguidorRepository;
    @Autowired
    private NovidadeRepository novidadeRepository;

    @BeforeEach
    void limparBanco() {
        jdbc.execute("TRUNCATE novidade, seguidor, cliente, loja RESTART IDENTITY CASCADE");
    }

    protected Loja novaLoja(String nome) {
        return lojaRepository.save(new Loja(nome));
    }

    protected Cliente novoCliente(String nome, String email) {
        return clienteRepository.save(new Cliente(nome, email));
    }

    protected void seguir(Cliente cliente, Loja loja) {
        seguidorRepository.save(new Seguidor(cliente.getId(), loja.getId()));
    }

    protected Novidade novaNovidade(Loja loja, String texto, Instant criadaEm) {
        return novidadeRepository.save(new Novidade(loja, texto, null, criadaEm));
    }

    protected long contar(String tabela) {
        Long total = jdbc.queryForObject("SELECT count(*) FROM " + tabela, Long.class);
        return total == null ? 0 : total;
    }
}
