package com.feednovidades.cliente;

public record ClienteResponse(Long id, String nome, String email) {

    static ClienteResponse de(Cliente cliente) {
        return new ClienteResponse(cliente.getId(), cliente.getNome(), cliente.getEmail());
    }
}
