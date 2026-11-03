package com.feednovidades.loja;

public record LojaResponse(Long id, String nome) {

    static LojaResponse de(Loja loja) {
        return new LojaResponse(loja.getId(), loja.getNome());
    }
}
