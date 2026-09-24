package br.com.dio.ecommerce.service;

import br.com.dio.ecommerce.dto.CategoriaRequest;
import br.com.dio.ecommerce.dto.CategoriaResponse;
import br.com.dio.ecommerce.exception.RecursoNaoEncontradoException;
import br.com.dio.ecommerce.exception.RegraNegocioException;
import br.com.dio.ecommerce.model.Categoria;
import br.com.dio.ecommerce.repository.CategoriaRepository;
import br.com.dio.ecommerce.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * {@code @Service} = bean singleton por padrão. Por isso a classe NÃO guarda estado
 * de requisição em campos: a mesma instância atende todas as threads ao mesmo tempo.
 * Os únicos campos são dependências imutáveis (final), também singletons.
 */
@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, ProdutoRepository produtoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.produtoRepository = produtoRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAll().stream().map(CategoriaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse buscar(Long id) {
        return CategoriaResponse.from(buscarEntidade(id));
    }

    @Transactional
    public CategoriaResponse criar(CategoriaRequest request) {
        if (categoriaRepository.existsByNomeIgnoreCase(request.nome())) {
            throw new RegraNegocioException("Já existe uma categoria com o nome '" + request.nome() + "'");
        }
        return CategoriaResponse.from(categoriaRepository.save(new Categoria(request.nome())));
    }

    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaRequest request) {
        Categoria categoria = buscarEntidade(id);
        if (categoriaRepository.existsByNomeIgnoreCaseAndIdNot(request.nome(), id)) {
            throw new RegraNegocioException("Já existe uma categoria com o nome '" + request.nome() + "'");
        }
        categoria.setNome(request.nome());
        return CategoriaResponse.from(categoria);
    }

    @Transactional
    public void excluir(Long id) {
        Categoria categoria = buscarEntidade(id);
        if (produtoRepository.existsByCategoriaId(id)) {
            throw new RegraNegocioException("Categoria possui produtos vinculados e não pode ser excluída");
        }
        categoriaRepository.delete(categoria);
    }

    Categoria buscarEntidade(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria", id));
    }
}
