package com.feednovidades;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import com.feednovidades.loja.Loja;

class PublicarNovidadeTest extends IntegracaoBase {

    @Test
    void publicaNovidadeComPreco() throws Exception {
        Loja loja = novaLoja("Loja");

        publicar(loja.getId(), json("Produto novo", "19.90"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.lojaId").value(loja.getId()))
                .andExpect(jsonPath("$.lojaNome").value("Loja"))
                .andExpect(jsonPath("$.texto").value("Produto novo"))
                .andExpect(jsonPath("$.preco").value(19.9))
                .andExpect(jsonPath("$.criadaEm").isString());

        assertThat(contar("novidade")).isEqualTo(1);
    }

    @Test
    void publicaNovidadeSemPreco() throws Exception {
        Loja loja = novaLoja("Loja");

        publicar(loja.getId(), "{\"texto\":\"Live hoje às 20h\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.preco").doesNotExist());
    }

    @Test
    void aceitaTextoComExatamente500Caracteres() throws Exception {
        Loja loja = novaLoja("Loja");

        publicar(loja.getId(), json("a".repeat(500), "null"))
                .andExpect(status().isCreated());
    }

    @Test
    void textoComMaisDe500CaracteresRetorna422() throws Exception {
        Loja loja = novaLoja("Loja");

        publicar(loja.getId(), json("a".repeat(501), "null"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.erro").value("VALIDACAO"))
                .andExpect(jsonPath("$.detalhes", hasItem("texto: deve ter no máximo 500 caracteres")));

        assertThat(contar("novidade")).isZero();
    }

    @Test
    void textoVazioEmBrancoOuAusenteRetorna422() throws Exception {
        Loja loja = novaLoja("Loja");

        publicar(loja.getId(), json("", "null"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detalhes", hasItem("texto: é obrigatório")));
        publicar(loja.getId(), json("   ", "null"))
                .andExpect(status().isUnprocessableEntity());
        publicar(loja.getId(), "{}")
                .andExpect(status().isUnprocessableEntity());

        assertThat(contar("novidade")).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-0.01", "-10"})
    void precoIgualOuMenorQueZeroRetorna422(String preco) throws Exception {
        Loja loja = novaLoja("Loja");

        publicar(loja.getId(), json("Promoção", preco))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detalhes", hasItem("preco: deve ser maior que zero")));

        assertThat(contar("novidade")).isZero();
    }

    @Test
    void precoComMaisDeDuasCasasDecimaisRetorna422() throws Exception {
        Loja loja = novaLoja("Loja");

        publicar(loja.getId(), json("Promoção", "1.234"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void lojaInexistenteRetorna404() throws Exception {
        publicar(999L, json("Produto novo", "10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("NAO_ENCONTRADO"));
    }

    @Test
    void jsonMalformadoRetorna422NoFormatoPadrao() throws Exception {
        Loja loja = novaLoja("Loja");

        publicar(loja.getId(), "{\"texto\":")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.erro").value("VALIDACAO"))
                .andExpect(jsonPath("$.detalhes").isArray());
    }

    private ResultActions publicar(Long lojaId, String corpo) throws Exception {
        return mvc.perform(post("/lojas/{id}/novidades", lojaId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo));
    }

    private static String json(String texto, String preco) {
        return "{\"texto\":\"%s\",\"preco\":%s}".formatted(texto, preco);
    }
}
