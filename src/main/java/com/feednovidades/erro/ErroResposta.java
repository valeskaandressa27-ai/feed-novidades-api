package com.feednovidades.erro;

import java.util.List;

/**
 * Formato único de erro de toda a API.
 *
 * @param status   código HTTP
 * @param erro     código estável do erro (VALIDACAO, NAO_ENCONTRADO, CONFLITO...)
 * @param mensagem texto legível
 * @param detalhes lista de detalhes (ex.: um item por campo inválido); pode ser vazia
 */
public record ErroResposta(int status, String erro, String mensagem, List<String> detalhes) {
}
