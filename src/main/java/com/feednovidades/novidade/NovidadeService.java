package com.feednovidades.novidade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.feednovidades.erro.RecursoNaoEncontradoException;
import com.feednovidades.loja.Loja;
import com.feednovidades.loja.LojaRepository;

@Service
public class NovidadeService {

    private final NovidadeRepository novidadeRepository;
    private final LojaRepository lojaRepository;

    public NovidadeService(NovidadeRepository novidadeRepository, LojaRepository lojaRepository) {
        this.novidadeRepository = novidadeRepository;
        this.lojaRepository = lojaRepository;
    }

    @Transactional
    public NovidadeResponse publicar(Long lojaId, NovidadeRequest request) {
        Loja loja = lojaRepository.findById(lojaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Loja " + lojaId + " não encontrada"));

        Novidade novidade = novidadeRepository.save(
                new Novidade(loja, request.texto().trim(), request.preco()));
        return NovidadeResponse.de(novidade);
    }
}
