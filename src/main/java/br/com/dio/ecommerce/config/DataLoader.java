package br.com.dio.ecommerce.config;

import br.com.dio.ecommerce.model.Categoria;
import br.com.dio.ecommerce.model.Cliente;
import br.com.dio.ecommerce.model.Produto;
import br.com.dio.ecommerce.repository.CategoriaRepository;
import br.com.dio.ecommerce.repository.ClienteRepository;
import br.com.dio.ecommerce.repository.ProdutoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Popula o banco H2 na inicialização. Usamos CommandLineRunner (e não data.sql) para que
 * os IDs sejam gerados pelo próprio banco e não conflitem com inserts feitos pela API.
 */
@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;
    private final ClienteRepository clienteRepository;

    public DataLoader(CategoriaRepository categoriaRepository, ProdutoRepository produtoRepository,
                      ClienteRepository clienteRepository) {
        this.categoriaRepository = categoriaRepository;
        this.produtoRepository = produtoRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (categoriaRepository.count() > 0) {
            return;
        }

        Categoria eletronicos = categoriaRepository.save(new Categoria("Eletrônicos"));
        Categoria livros = categoriaRepository.save(new Categoria("Livros"));
        Categoria casa = categoriaRepository.save(new Categoria("Casa e Cozinha"));

        produtoRepository.saveAll(List.of(
                new Produto("Notebook Pro 14", "Notebook com 16GB RAM e SSD 512GB",
                        new BigDecimal("4599.90"), 10, eletronicos),
                new Produto("Mouse sem fio", "Mouse óptico 2.4GHz", new BigDecimal("89.90"), 50, eletronicos),
                new Produto("Fone Bluetooth", "Fone over-ear com cancelamento de ruído",
                        new BigDecimal("349.90"), 25, eletronicos),
                new Produto("Clean Code", "Robert C. Martin", new BigDecimal("119.90"), 30, livros),
                new Produto("Design Patterns (GoF)", "Gamma, Helm, Johnson e Vlissides",
                        new BigDecimal("159.90"), 15, livros),
                new Produto("Cafeteira Elétrica", "Cafeteira 30 xícaras", new BigDecimal("229.90"), 8, casa),
                new Produto("Caneca Java", "Caneca de cerâmica 350ml", new BigDecimal("39.90"), 100, casa)));

        clienteRepository.saveAll(List.of(
                new Cliente("Ana Souza", "ana.souza@email.com", "52998224725"),
                new Cliente("Bruno Lima", "bruno.lima@email.com", "12345678909"),
                new Cliente("Carla Mendes", "carla.mendes@email.com", "11144477735")));

        log.info("Dados iniciais carregados: {} categorias, {} produtos, {} clientes",
                categoriaRepository.count(), produtoRepository.count(), clienteRepository.count());
    }
}
