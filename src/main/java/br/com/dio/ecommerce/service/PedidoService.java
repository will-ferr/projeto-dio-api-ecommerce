package br.com.dio.ecommerce.service;

import br.com.dio.ecommerce.dto.ItemPedidoRequest;
import br.com.dio.ecommerce.dto.PedidoRequest;
import br.com.dio.ecommerce.dto.PedidoResponse;
import br.com.dio.ecommerce.exception.RecursoNaoEncontradoException;
import br.com.dio.ecommerce.exception.RegraNegocioException;
import br.com.dio.ecommerce.model.Cliente;
import br.com.dio.ecommerce.model.ItemPedido;
import br.com.dio.ecommerce.model.Pedido;
import br.com.dio.ecommerce.model.Produto;
import br.com.dio.ecommerce.model.StatusPedido;
import br.com.dio.ecommerce.repository.ClienteRepository;
import br.com.dio.ecommerce.repository.PedidoRepository;
import br.com.dio.ecommerce.repository.ProdutoRepository;
import br.com.dio.ecommerce.singleton.ConfiguracaoLoja;
import br.com.dio.ecommerce.singleton.GeradorNumeroPedido;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Onde os dois "mundos" do Singleton se encontram:
 * <ul>
 *   <li>Este service é um <b>singleton do Spring</b> (uma instância por ApplicationContext).</li>
 *   <li>Ele usa dois <b>singletons clássicos</b> — {@link ConfiguracaoLoja} e
 *       {@link GeradorNumeroPedido} — recebidos por INJEÇÃO no construtor
 *       (registrados como beans em {@code SingletonConfig}), e não via {@code getInstance()}
 *       espalhado pelo código. Isso mantém o service desacoplado e testável.</li>
 * </ul>
 */
@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final ConfiguracaoLoja configuracaoLoja;
    private final GeradorNumeroPedido geradorNumeroPedido;

    public PedidoService(PedidoRepository pedidoRepository,
                         ClienteRepository clienteRepository,
                         ProdutoRepository produtoRepository,
                         ConfiguracaoLoja configuracaoLoja,
                         GeradorNumeroPedido geradorNumeroPedido) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
        this.configuracaoLoja = configuracaoLoja;
        this.geradorNumeroPedido = geradorNumeroPedido;
    }

    /**
     * Cria o pedido: valida cliente e produtos, baixa o estoque, aplica as regras da
     * {@link ConfiguracaoLoja} (valor mínimo e frete) e numera com o {@link GeradorNumeroPedido}.
     * Tudo em uma transação: se qualquer validação falhar, a baixa de estoque é desfeita.
     */
    @Transactional
    public PedidoResponse criar(PedidoRequest request) {
        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", request.clienteId()));

        Pedido pedido = new Pedido(geradorNumeroPedido.proximoNumero(), cliente);

        for (ItemPedidoRequest itemRequest : request.itens()) {
            Produto produto = produtoRepository.findById(itemRequest.produtoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", itemRequest.produtoId()));

            if (!produto.temEstoque(itemRequest.quantidade())) {
                throw new RegraNegocioException("Estoque insuficiente para '%s': solicitado %d, disponível %d"
                        .formatted(produto.getNome(), itemRequest.quantidade(), produto.getEstoque()));
            }
            produto.baixarEstoque(itemRequest.quantidade());
            pedido.adicionarItem(new ItemPedido(produto, itemRequest.quantidade()));
        }

        BigDecimal subtotal = pedido.getSubtotal();
        if (!configuracaoLoja.atingeValorMinimo(subtotal)) {
            throw new RegraNegocioException("Valor mínimo do pedido é %s %s; subtotal atual: %s %s".formatted(
                    configuracaoLoja.getMoeda(), configuracaoLoja.getValorMinimoPedido(),
                    configuracaoLoja.getMoeda(), subtotal));
        }
        pedido.aplicarFrete(configuracaoLoja.calcularFrete(subtotal));

        return PedidoResponse.from(pedidoRepository.save(pedido));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listar() {
        return pedidoRepository.findAllByOrderByIdDesc().stream().map(PedidoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscar(Long id) {
        return PedidoResponse.from(buscarEntidade(id));
    }

    @Transactional
    public PedidoResponse atualizarStatus(Long id, StatusPedido novoStatus) {
        Pedido pedido = buscarEntidade(id);
        StatusPedido atual = pedido.getStatus();

        if (!atual.podeMudarPara(novoStatus)) {
            throw new RegraNegocioException("Transição de status inválida: %s -> %s. Permitidas: %s"
                    .formatted(atual, novoStatus, atual.proximosPermitidos()));
        }
        if (novoStatus == StatusPedido.CANCELADO) {
            pedido.getItens().forEach(item -> item.getProduto().devolverEstoque(item.getQuantidade()));
        }
        pedido.alterarStatus(novoStatus);
        return PedidoResponse.from(pedido);
    }

    private Pedido buscarEntidade(Long id) {
        return pedidoRepository.findComItensById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", id));
    }
}
