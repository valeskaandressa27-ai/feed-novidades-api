package com.feednovidades;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.feednovidades.cliente.Cliente;
import com.feednovidades.loja.Loja;

class SeguirLojaTest extends IntegracaoBase {

    @Test
    void clienteSegueLojaComSucesso() throws Exception {
        Cliente ana = novoCliente("Ana", "ana@teste.com");
        Loja loja = novaLoja("Loja");

        mvc.perform(post("/clientes/{id}/seguindo/{lojaId}", ana.getId(), loja.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clienteId").value(ana.getId()))
                .andExpect(jsonPath("$.lojaId").value(loja.getId()));

        assertThat(contar("seguidor")).isEqualTo(1);
    }

    @Test
    void clienteInexistenteRetorna404() throws Exception {
        Loja loja = novaLoja("Loja");

        mvc.perform(post("/clientes/{id}/seguindo/{lojaId}", 999, loja.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("NAO_ENCONTRADO"));
    }

    @Test
    void lojaInexistenteRetorna404() throws Exception {
        Cliente ana = novoCliente("Ana", "ana@teste.com");

        mvc.perform(post("/clientes/{id}/seguindo/{lojaId}", ana.getId(), 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("NAO_ENCONTRADO"));
    }

    @Test
    void seguirDuasVezesRetorna409() throws Exception {
        Cliente ana = novoCliente("Ana", "ana@teste.com");
        Loja loja = novaLoja("Loja");

        mvc.perform(post("/clientes/{id}/seguindo/{lojaId}", ana.getId(), loja.getId()))
                .andExpect(status().isCreated());
        mvc.perform(post("/clientes/{id}/seguindo/{lojaId}", ana.getId(), loja.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.erro").value("CONFLITO"));

        assertThat(contar("seguidor")).isEqualTo(1);
    }

    @Test
    void requisicoesSimultaneasParaOMesmoParGeramUmSucessoEOsDemais409() throws Exception {
        Cliente ana = novoCliente("Ana", "ana@teste.com");
        Loja loja = novaLoja("Loja");
        int tentativas = 8;

        ExecutorService pool = Executors.newFixedThreadPool(tentativas);
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<Integer>> resultados = new ArrayList<>();
        for (int i = 0; i < tentativas; i++) {
            resultados.add(pool.submit(() -> {
                largada.await();
                return mvc.perform(post("/clientes/{id}/seguindo/{lojaId}", ana.getId(), loja.getId()))
                        .andReturn().getResponse().getStatus();
            }));
        }
        largada.countDown();

        List<Integer> codigos = new ArrayList<>();
        for (Future<Integer> resultado : resultados) {
            codigos.add(resultado.get(30, TimeUnit.SECONDS));
        }
        pool.shutdown();

        assertThat(codigos).filteredOn(c -> c == 201).hasSize(1);
        assertThat(codigos).filteredOn(c -> c == 409).hasSize(tentativas - 1);
        assertThat(contar("seguidor")).isEqualTo(1);
    }
}
