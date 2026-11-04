package com.feednovidades.loja;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LojaService {

    private final LojaRepository lojaRepository;

    public LojaService(LojaRepository lojaRepository) {
        this.lojaRepository = lojaRepository;
    }

    @Transactional
    public LojaResponse criar(LojaRequest request) {
        Loja loja = lojaRepository.save(new Loja(request.nome().trim()));
        return LojaResponse.de(loja);
    }
}
