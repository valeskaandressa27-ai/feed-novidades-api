package com.feednovidades.seguidor;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class SeguidorId implements Serializable {

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "loja_id", nullable = false)
    private Long lojaId;

    protected SeguidorId() {
    }

    public SeguidorId(Long clienteId, Long lojaId) {
        this.clienteId = clienteId;
        this.lojaId = lojaId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public Long getLojaId() {
        return lojaId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SeguidorId outro)) {
            return false;
        }
        return Objects.equals(clienteId, outro.clienteId) && Objects.equals(lojaId, outro.lojaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clienteId, lojaId);
    }
}
