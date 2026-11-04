package com.feednovidades;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

class ClienteTest extends IntegracaoBase {

    @Test
    void criaClienteENormalizaEmail() throws Exception {
        criar("{\"nome\":\"Ana\",\"email\":\"Ana@Teste.com\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Ana"))
                .andExpect(jsonPath("$.email").value("ana@teste.com"));
    }

    @Test
    void emailDuplicadoRetorna409() throws Exception {
        criar("{\"nome\":\"Ana\",\"email\":\"ana@teste.com\"}").andExpect(status().isCreated());

        criar("{\"nome\":\"Outra Ana\",\"email\":\"ana@teste.com\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.erro").value("CONFLITO"));

        assertThat(contar("cliente")).isEqualTo(1);
    }

    @Test
    void emailDuplicadoIgnoraDiferencaDeCaixa() throws Exception {
        criar("{\"nome\":\"Ana\",\"email\":\"ana@teste.com\"}").andExpect(status().isCreated());

        criar("{\"nome\":\"Ana\",\"email\":\"ANA@TESTE.COM\"}")
                .andExpect(status().isConflict());
    }

    @Test
    void bancoGarantaUnicidadeDoEmail() {
        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO cliente (nome, email) VALUES ('x', 'dup@teste.com'), ('y', 'dup@teste.com')"))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void camposObrigatoriosRetornam422() throws Exception {
        criar("{\"email\":\"ana@teste.com\"}")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detalhes", hasItem("nome: é obrigatório")));
        criar("{\"nome\":\"Ana\"}")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detalhes", hasItem("email: é obrigatório")));
        criar("{\"nome\":\"  \",\"email\":\"\"}")
                .andExpect(status().isUnprocessableEntity());

        assertThat(contar("cliente")).isZero();
    }

    @Test
    void emailInvalidoRetorna422() throws Exception {
        criar("{\"nome\":\"Ana\",\"email\":\"isso-nao-e-email\"}")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detalhes", hasItem("email: deve ser um e-mail válido")));
    }

    private ResultActions criar(String corpo) throws Exception {
        return mvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content(corpo));
    }
}
