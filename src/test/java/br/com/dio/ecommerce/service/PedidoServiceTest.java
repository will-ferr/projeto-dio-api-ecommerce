package br.com.dio.ecommerce.service;

import br.com.dio.ecommerce.dto.ItemPedidoRequest;
import br.com.dio.ecommerce.dto.PedidoRequest;
import br.com.dio.ecommerce.dto.PedidoResponse;
import br.com.dio.ecommerce.exception.RecursoNaoEncontradoException;
import br.com.dio.ecommerce.exception.RegraNegocioException;
import br.com.dio.ecommerce.model.Categoria;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Teste unitário do fluxo de pedido, sem subir o Spring. Como o service recebe os
 * singletons pelo construtor, basta passar as instâncias — sem precisar de hacks
 * de reflection para substituir um getInstance() estático.
 */
@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private ProdutoRepository produtoRepository;

    private PedidoService service;
    private Cliente cliente;
    private Produto livro;
    private Produto caneca;

    @BeforeEach
    void setUp() {
        service = new PedidoService(pedidoRepository, clienteRepository, produtoRepository,
                ConfiguracaoLoja.getInstance(), GeradorNumeroPedido.getInstance());

        Categoria categoria = new Categoria("Livros");
        cliente = comId(new Cliente("Ana", "ana@email.com", "52998224725"), 1L);
        livro = comId(new Produto("Clean Code", "", new BigDecimal("100.00"), 5, categoria), 10L);
        caneca = comId(new Produto("Caneca", "", new BigDecimal("20.00"), 3, categoria), 20L);
    }

    @Test
    @DisplayName("Cria pedido: baixa estoque, calcula subtotal + frete e gera número")
    void criaPedidoComSucesso() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(produtoRepository.findById(10L)).thenReturn(Optional.of(livro));
        when(produtoRepository.findById(20L)).thenReturn(Optional.of(caneca));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        PedidoResponse resposta = service.criar(new PedidoRequest(1L, List.of(
                new ItemPedidoRequest(10L, 2),
                new ItemPedidoRequest(20L, 1))));

        // 2 x 100,00 + 1 x 20,00 = 220,00 ; frete fixo 15,90 ; total 235,90
        assertThat(resposta.subtotal()).isEqualByComparingTo("220.00");
        assertThat(resposta.valorFrete()).isEqualByComparingTo("15.90");
        assertThat(resposta.valorTotal()).isEqualByComparingTo("235.90");
        assertThat(resposta.status()).isEqualTo(StatusPedido.CRIADO);
        assertThat(resposta.numero()).startsWith("PED-");
        assertThat(resposta.itens()).hasSize(2);
        assertThat(livro.getEstoque()).isEqualTo(3);
        assertThat(caneca.getEstoque()).isEqualTo(2);
    }

    @Test
    @DisplayName("Rejeita pedido com estoque insuficiente (422)")
    void rejeitaEstoqueInsuficiente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(produtoRepository.findById(10L)).thenReturn(Optional.of(livro));

        assertThatThrownBy(() -> service.criar(new PedidoRequest(1L, List.of(new ItemPedidoRequest(10L, 6)))))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Estoque insuficiente");
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rejeita pedido abaixo do valor mínimo da ConfiguracaoLoja (422)")
    void rejeitaAbaixoDoValorMinimo() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(produtoRepository.findById(20L)).thenReturn(Optional.of(caneca));

        // 2 x 20,00 = 40,00 < mínimo de 50,00
        assertThatThrownBy(() -> service.criar(new PedidoRequest(1L, List.of(new ItemPedidoRequest(20L, 2)))))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Valor mínimo");
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rejeita pedido de cliente inexistente (404)")
    void rejeitaClienteInexistente() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(new PedidoRequest(99L, List.of(new ItemPedidoRequest(10L, 1)))))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Cancelar pedido devolve itens ao estoque")
    void cancelarDevolveEstoque() {
        Pedido pedido = new Pedido("PED-TESTE", cliente);
        livro.baixarEstoque(2);
        pedido.adicionarItem(new ItemPedido(livro, 2));
        when(pedidoRepository.findComItensById(1L)).thenReturn(Optional.of(pedido));

        PedidoResponse resposta = service.atualizarStatus(1L, StatusPedido.CANCELADO);

        assertThat(resposta.status()).isEqualTo(StatusPedido.CANCELADO);
        assertThat(livro.getEstoque()).isEqualTo(5);
    }

    @Test
    @DisplayName("Rejeita transição de status inválida (422)")
    void rejeitaTransicaoInvalida() {
        Pedido pedido = new Pedido("PED-TESTE", cliente);
        when(pedidoRepository.findComItensById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> service.atualizarStatus(1L, StatusPedido.ENTREGUE))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("CRIADO -> ENTREGUE");
    }

    private static <T> T comId(T entidade, Long id) {
        ReflectionTestUtils.setField(entidade, "id", id);
        return entidade;
    }
}
