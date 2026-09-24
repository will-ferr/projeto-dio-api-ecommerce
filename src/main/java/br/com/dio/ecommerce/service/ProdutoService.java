package br.com.dio.ecommerce.service;

import br.com.dio.ecommerce.dto.ProdutoRequest;
import br.com.dio.ecommerce.dto.ProdutoResponse;
import br.com.dio.ecommerce.exception.RecursoNaoEncontradoException;
import br.com.dio.ecommerce.exception.RegraNegocioException;
import br.com.dio.ecommerce.model.Categoria;
import br.com.dio.ecommerce.model.Produto;
import br.com.dio.ecommerce.repository.PedidoRepository;
import br.com.dio.ecommerce.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final PedidoRepository pedidoRepository;
    private final CategoriaService categoriaService;

    public ProdutoService(ProdutoRepository produtoRepository, PedidoRepository pedidoRepository,
                          CategoriaService categoriaService) {
        this.produtoRepository = produtoRepository;
        this.pedidoRepository = pedidoRepository;
        this.categoriaService = categoriaService;
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listar() {
        return produtoRepository.findAll().stream().map(ProdutoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscar(Long id) {
        return ProdutoResponse.from(buscarEntidade(id));
    }

    @Transactional
    public ProdutoResponse criar(ProdutoRequest request) {
        Categoria categoria = categoriaService.buscarEntidade(request.categoriaId());
        Produto produto = new Produto(
                request.nome(), request.descricao(), request.preco(), request.estoque(), categoria);
        return ProdutoResponse.from(produtoRepository.save(produto));
    }

    @Transactional
    public ProdutoResponse atualizar(Long id, ProdutoRequest request) {
        Produto produto = buscarEntidade(id);
        produto.setNome(request.nome());
        produto.setDescricao(request.descricao());
        produto.setPreco(request.preco());
        produto.setEstoque(request.estoque());
        produto.setCategoria(categoriaService.buscarEntidade(request.categoriaId()));
        return ProdutoResponse.from(produto);
    }

    @Transactional
    public void excluir(Long id) {
        Produto produto = buscarEntidade(id);
        if (pedidoRepository.existsByItensProdutoId(id)) {
            throw new RegraNegocioException("Produto já consta em pedidos e não pode ser excluído");
        }
        produtoRepository.delete(produto);
    }

    Produto buscarEntidade(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", id));
    }
}
