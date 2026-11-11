package com.feednovidades.novidade;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NovidadeController {

    private final NovidadeService novidadeService;

    public NovidadeController(NovidadeService novidadeService) {
        this.novidadeService = novidadeService;
    }

    @PostMapping("/lojas/{id}/novidades")
    @ResponseStatus(HttpStatus.CREATED)
    public NovidadeResponse publicar(@PathVariable("id") Long lojaId,
                                     @Valid @RequestBody NovidadeRequest request) {
        return novidadeService.publicar(lojaId, request);
    }
}
