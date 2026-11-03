package com.feednovidades.seguidor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.feednovidades.cliente.ClienteRepository;
import com.feednovidades.erro.ConflitoException;
import com.feednovidades.erro.RecursoNaoEncontradoException;
import com.feednovidades.loja.LojaRepository;

@Service
public class SeguidorService {

    private static final String JA_SEGUE = "O cliente já segue esta loja";

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
        if (seguidorRepository.existsById(new SeguidorId(clienteId, lojaId))) {
            throw new ConflitoException(JA_SEGUE);
        }
        try {
            seguidorRepository.saveAndFlush(new Seguidor(clienteId, lojaId));
        } catch (DataIntegrityViolationException e) {
            // Corrida: a PK composta barrou um insert simultâneo do mesmo par.
            throw new ConflitoException(JA_SEGUE);
        }
        return new SeguidorResponse(clienteId, lojaId);
    }
}
