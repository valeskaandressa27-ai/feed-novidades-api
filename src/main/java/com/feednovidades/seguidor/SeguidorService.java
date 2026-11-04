package com.feednovidades.seguidor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.feednovidades.cliente.ClienteRepository;
import com.feednovidades.erro.ConflitoException;
import com.feednovidades.erro.RecursoNaoEncontradoException;
import com.feednovidades.loja.LojaRepository;

@Service
public class SeguidorService {

    private final SeguidorRepository seguidorRepository;
    private final ClienteRepository clienteRepository;
    private final LojaRepository lojaRepository;

    public SeguidorService(SeguidorRepository seguidorRepository,
                           ClienteRepository clienteRepository,
                           LojaRepository lojaRepository) {
        this.seguidorRepository = seguidorRepository;
        this.clienteRepository = clienteRepository;
        this.lojaRepository = lojaRepository;
    }

    @Transactional
    public SeguidorResponse seguir(Long clienteId, Long lojaId) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new RecursoNaoEncontradoException("Cliente " + clienteId + " não encontrado");
        }
        if (!lojaRepository.existsById(lojaId)) {
            throw new RecursoNaoEncontradoException("Loja " + lojaId + " não encontrada");
        }

        // A decisão "já existe?" é feita pelo banco, atomicamente: 0 linhas inseridas = já seguia.
        // Isso vale também para requisições simultâneas do mesmo par (só uma insere).
        int inseridas = seguidorRepository.inserirSeNaoExistir(clienteId, lojaId);
        if (inseridas == 0) {
            throw new ConflitoException("O cliente já segue esta loja");
        }
        return new SeguidorResponse(clienteId, lojaId);
    }
}    
