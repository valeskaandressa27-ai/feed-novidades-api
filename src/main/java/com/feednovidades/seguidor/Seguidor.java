package com.feednovidades.seguidor;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "seguidor")
public class Seguidor {

    @EmbeddedId
    private SeguidorId id;

    protected Seguidor() {
    }

    public Seguidor(Long clienteId, Long lojaId) {
        this.id = new SeguidorId(clienteId, lojaId);
    }

    public SeguidorId getId() {
        return id;
    }
}
