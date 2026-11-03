package com.feednovidades.feed;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.feednovidades.novidade.NovidadeResponse;

@RestController
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping("/clientes/{id}/feed")
    public List<NovidadeResponse> feed(@PathVariable("id") Long clienteId,
                                       @RequestParam(required = false) Integer limite) {
        return feedService.feed(clienteId, limite);
    }
}
