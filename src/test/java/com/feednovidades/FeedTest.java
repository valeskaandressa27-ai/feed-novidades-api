package com.feednovidades;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

import com.feednovidades.cliente.Cliente;
import com.feednovidades.loja.Loja;

class FeedTest extends IntegracaoBase {

    private static final Instant BASE = Instant.parse("2026-01-01T10:00:00Z");

    @Test
    void retornaSomenteNovidadesDeLojasSeguidas() throws Exception {
        Cliente ana = novoCliente("Ana", "ana@teste.com");
        Loja seguida = novaLoja("Seguida");
        Loja naoSeguida = novaLoja("Não seguida");
        seguir(ana, seguida);
        novaNovidade(seguida, "da loja seguida", BASE);
        novaNovidade(naoSeguida, "da loja não seguida", BASE.plusSeconds(60));

        mvc.perform(get("/clientes/{id}/feed", ana.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].texto").value("da loja seguida"))
                .andExpect(jsonPath("$[0].lojaId").value(seguida.getId()))
                .andExpect(jsonPath("$[0].lojaNome").value("Seguida"));
    }

    @Test
    void naoMostraNovidadesDeLojasSeguidasPorOutroCliente() throws Exception {
        Cliente ana = novoCliente("Ana", "ana@teste.com");
        Cliente bruno = novoCliente("Bruno", "bruno@teste.com");
        Loja loja = novaLoja("Loja");
        seguir(bruno, loja);
        novaNovidade(loja, "novidade", BASE);

        mvc.perform(get("/clientes/{id}/feed", ana.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void ordenaDaMaisRecenteParaAMaisAntigaComDesempatePorId() throws Exception {
        Cliente ana = novoCliente("Ana", "ana@teste.com");
        Loja a = novaLoja("A");
        Loja b = novaLoja("B");
        seguir(ana, a);
        seguir(ana, b);
        novaNovidade(a, "antiga", BASE);
        novaNovidade(b, "empate-1", BASE.plus(1, ChronoUnit.HOURS));
        novaNovidade(a, "empate-2", BASE.plus(1, ChronoUnit.HOURS)); // mesmo instante, id maior
        novaNovidade(b, "recente", BASE.plus(2, ChronoUnit.HOURS));

        mvc.perform(get("/clientes/{id}/feed", ana.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].texto", contains("recente", "empate-2", "empate-1", "antiga")));
    }

    @Test
    void usaLimitePadraoDe20() throws Exception {
        Cliente ana = novoCliente("Ana", "ana@teste.com");
        Loja loja = novaLoja("Loja");
        seguir(ana, loja);
        criarNovidades(loja, 25);

        mvc.perform(get("/clientes/{id}/feed", ana.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(20)))
                .andExpect(jsonPath("$[0].texto").value("novidade 25"))
                .andExpect(jsonPath("$[19].texto").value("novidade 6"));
    }

    @Test
    void respeitaLimiteInformadoEMaximoDe50() throws Exception {
        Cliente ana = novoCliente("Ana", "ana@teste.com");
        Loja loja = novaLoja("Loja");
        seguir(ana, loja);
        criarNovidades(loja, 60);

        mvc.perform(get("/clientes/{id}/feed", ana.getId()).param("limite", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));

        mvc.perform(get("/clientes/{id}/feed", ana.getId()).param("limite", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(50)));

        // Acima do máximo: limitado a 50 (decisão documentada no README).
        mvc.perform(get("/clientes/{id}/feed", ana.getId()).param("limite", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(50)));
    }

    @Test
    void limiteMenorQueUmRetorna422() throws Exception {
        Cliente ana = novoCliente("Ana", "ana@teste.com");

        mvc.perform(get("/clientes/{id}/feed", ana.getId()).param("limite", "0"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.erro").value("VALIDACAO"));

        mvc.perform(get("/clientes/{id}/feed", ana.getId()).param("limite", "-3"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void limiteNaoNumericoRetorna422() throws Exception {
        Cliente ana = novoCliente("Ana", "ana@teste.com");

        mvc.perform(get("/clientes/{id}/feed", ana.getId()).param("limite", "abc"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.erro").value("VALIDACAO"));
    }

    @Test
    void clienteInexistenteRetorna404NoFormatoPadrao() throws Exception {
        mvc.perform(get("/clientes/{id}/feed", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("NAO_ENCONTRADO"))
                .andExpect(jsonPath("$.mensagem").isString())
                .andExpect(jsonPath("$.detalhes").isArray());
    }

    @Test
    void clienteQueNaoSegueNinguemRecebeListaVazia() throws Exception {
        Cliente ana = novoCliente("Ana", "ana@teste.com");

        mvc.perform(get("/clientes/{id}/feed", ana.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    private void criarNovidades(Loja loja, int quantidade) {
        for (int i = 1; i <= quantidade; i++) {
            novaNovidade(loja, "novidade " + i, BASE.plus(i, ChronoUnit.MINUTES));
        }
    }
}
