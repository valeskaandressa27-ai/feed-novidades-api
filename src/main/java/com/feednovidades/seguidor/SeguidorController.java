package com.feednovidades.seguidor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SeguidorController {

    private final SeguidorService seguidorService;

    public SeguidorController(SeguidorService seguidorService) {
        this.seguidorService = seguidorService;
    }

    @PostMapping("/clientes/{id}/seguindo/{lojaId}")
    @ResponseStatus(HttpStatus.CREATED)
    public SeguidorResponse seguir(@PathVariable("id") Long clienteId, @PathVariable Long lojaId) {
        return seguidorService.seguir(clienteId, lojaId);
    }
}
