package com.feednovidades.seed;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.feednovidades.cliente.Cliente;
import com.feednovidades.cliente.ClienteRepository;
import com.feednovidades.loja.Loja;
import com.feednovidades.loja.LojaRepository;
import com.feednovidades.novidade.Novidade;
import com.feednovidades.novidade.NovidadeRepository;
import com.feednovidades.seguidor.Seguidor;
import com.feednovidades.seguidor.SeguidorRepository;

/**
 * Carga de dados para teste manual. Só roda com o perfil "seed" e só se o banco
 * estiver vazio:  mvn spring-boot:run -Dspring-boot.run.profiles=seed
 */
@Component
@Profile("seed")
public class SeedRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedRunner.class);

    private final LojaRepository lojaRepository;
    private final ClienteRepository clienteRepository;
    private final SeguidorRepository seguidorRepository;
    private final NovidadeRepository novidadeRepository;

    public SeedRunner(LojaRepository lojaRepository, ClienteRepository clienteRepository,
                      SeguidorRepository seguidorRepository, NovidadeRepository novidadeRepository) {
        this.lojaRepository = lojaRepository;
        this.clienteRepository = clienteRepository;
        this.seguidorRepository = seguidorRepository;
        this.novidadeRepository = novidadeRepository;
    }

    @Override
    public void run(String... args) {
        if (lojaRepository.count() > 0 || clienteRepository.count() > 0) {
            log.info("Seed ignorado: o banco já possui dados.");
            return;
        }

        Loja cafeteria = lojaRepository.save(new Loja("Cafeteria Aurora"));
        Loja moda = lojaRepository.save(new Loja("Moda Brisa"));
        Loja livraria = lojaRepository.save(new Loja("Livraria Ponto e Vírgula"));

        Cliente ana = clienteRepository.save(new Cliente("Ana Souza", "ana@exemplo.com"));
        Cliente bruno = clienteRepository.save(new Cliente("Bruno Lima", "bruno@exemplo.com"));
        Cliente carla = clienteRepository.save(new Cliente("Carla Dias", "carla@exemplo.com"));

        // Ana segue cafeteria e moda; Bruno segue moda e livraria; Carla segue as três.
        seguir(ana, cafeteria);
        seguir(ana, moda);
        seguir(bruno, moda);
        seguir(bruno, livraria);
        seguir(carla, cafeteria);
        seguir(carla, moda);
        seguir(carla, livraria);

        Instant agora = Instant.now().truncatedTo(ChronoUnit.MICROS);
        novidade(cafeteria, "Novo blend de café da casa chegou!", "32.90", agora.minus(30, ChronoUnit.HOURS));
        novidade(moda, "Coleção de verão com 20% de desconto", "89.90", agora.minus(26, ChronoUnit.HOURS));
        novidade(livraria, "Live de lançamento amanhã às 19h", null, agora.minus(20, ChronoUnit.HOURS));
        novidade(cafeteria, "Promoção: café + pão de queijo", "14.50", agora.minus(12, ChronoUnit.HOURS));
        novidade(moda, "Chegaram as novas sandálias", "129.00", agora.minus(8, ChronoUnit.HOURS));
        novidade(livraria, "Clube de leitura abre inscrições", null, agora.minus(4, ChronoUnit.HOURS));
        novidade(cafeteria, "Hoje tem música ao vivo", null, agora.minus(1, ChronoUnit.HOURS));

        log.info("Seed concluído: 3 lojas, 3 clientes, 7 seguidores e 7 novidades.");
    }

    private void seguir(Cliente cliente, Loja loja) {
        seguidorRepository.save(new Seguidor(cliente.getId(), loja.getId()));
    }

    private void novidade(Loja loja, String texto, String preco, Instant criadaEm) {
        BigDecimal valor = preco == null ? null : new BigDecimal(preco);
        novidadeRepository.save(new Novidade(loja, texto, valor, criadaEm));
    }
}
