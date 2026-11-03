package com.feednovidades;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

class LojaTest extends IntegracaoBase {

    @Test
    void criaLoja() throws Exception {
        criar("{\"nome\":\"Cafeteria Aurora\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Cafeteria Aurora"));

        assertThat(contar("loja")).isEqualTo(1);
    }

    @Test
    void nomeObrigatorioRetorna422() throws Exception {
        criar("{}")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detalhes", hasItem("nome: é obrigatório")));
        criar("{\"nome\":\"   \"}")
                .andExpect(status().isUnprocessableEntity());

        assertThat(contar("loja")).isZero();
    }

    @Test
    void rotaInexistenteRetornaErroNoFormatoPadrao() throws Exception {
        mvc.perform(get("/rota-que-nao-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("NAO_ENCONTRADO"));
    }

    @Test
    void metodoNaoPermitidoRetornaErroNoFormatoPadrao() throws Exception {
        mvc.perform(get("/lojas"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.erro").value("REQUISICAO_INVALIDA"));
    }

    private ResultActions criar(String corpo) throws Exception {
        return mvc.perform(post("/lojas").contentType(MediaType.APPLICATION_JSON).content(corpo));
    }
}
