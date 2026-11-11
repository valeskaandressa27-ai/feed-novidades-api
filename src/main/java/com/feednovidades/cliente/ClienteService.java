package com.feednovidades.cliente;

import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.feednovidades.erro.ConflitoException;

@Service
public class ClienteService {

    private static final String EMAIL_DUPLICADO = "Já existe um cliente com este e-mail";

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public ClienteResponse criar(ClienteRequest request) {
        // E-mail normalizado (minúsculas, sem espaços) para a unicidade não depender de caixa.
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (clienteRepository.existsByEmail(email)) {
            throw new ConflitoException(EMAIL_DUPLICADO);
        }
        try {
            Cliente cliente = clienteRepository.saveAndFlush(new Cliente(request.nome().trim(), email));
            return ClienteResponse.de(cliente);
        } catch (DataIntegrityViolationException e) {
            // Corrida: outra requisição inseriu o mesmo e-mail entre a checagem e o insert.
            throw new ConflitoException(EMAIL_DUPLICADO);
        }
    }
}
