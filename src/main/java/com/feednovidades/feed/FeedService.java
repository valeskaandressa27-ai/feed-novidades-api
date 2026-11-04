package com.feednovidades.feed;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.feednovidades.cliente.ClienteRepository;
import com.feednovidades.erro.RecursoNaoEncontradoException;
import com.feednovidades.erro.ValidacaoException;
import com.feednovidades.novidade.NovidadeRepository;
import com.feednovidades.novidade.NovidadeResponse;

@Service
public class FeedService {

    static final int LIMITE_PADRAO = 20;
    static final int LIMITE_MAXIMO = 50;

    private final NovidadeRepository novidadeRepository;
    private final ClienteRepository clienteRepository;

    public FeedService(NovidadeRepository novidadeRepository, ClienteRepository clienteRepository) {
        this.novidadeRepository = novidadeRepository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public List<NovidadeResponse> feed(Long clienteId, Integer limite) {
        int tamanho = resolverLimite(limite);
        if (!clienteRepository.existsById(clienteId)) {
            throw new RecursoNaoEncontradoException("Cliente " + clienteId + " não encontrado");
        }
        return novidadeRepository.buscarFeed(clienteId, PageRequest.of(0, tamanho));
    }

    /** Ausente: 20. Menor que 1: 422. Maior que 50: limitado a 50. */
    private static int resolverLimite(Integer limite) {
        if (limite == null) {
            return LIMITE_PADRAO;
        }
        if (limite < 1) {
            throw new ValidacaoException("limite deve ser maior ou igual a 1");
        }
        return Math.min(limite, LIMITE_MAXIMO);
    }
}
