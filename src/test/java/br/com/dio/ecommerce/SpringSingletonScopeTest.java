package br.com.dio.ecommerce;

import br.com.dio.ecommerce.controller.PedidoController;
import br.com.dio.ecommerce.repository.PedidoRepository;
import br.com.dio.ecommerce.service.PedidoService;
import br.com.dio.ecommerce.singleton.ConfiguracaoLoja;
import br.com.dio.ecommerce.singleton.GeradorNumeroPedido;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prova que o container do Spring trata os beans como singletons (uma instância por
 * ApplicationContext) e contrasta com o escopo "prototype".
 */
@SpringBootTest
@AutoConfigureTestDatabase // banco H2 com nome único por contexto de teste
class SpringSingletonScopeTest {

    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired
    private PedidoService pedidoServiceInjetado;

    /** Bean prototype apenas para contraste: cada getBean cria um objeto novo. */
    static class CarrinhoTemporario {
    }

    @TestConfiguration
    static class PrototypeConfig {
        @Bean
        @Scope("prototype")
        CarrinhoTemporario carrinhoTemporario() {
            return new CarrinhoTemporario();
        }
    }

    @Test
    @DisplayName("getBean chamado duas vezes retorna a mesma instância do service")
    void serviceEhSingletonNoContainer() {
        PedidoService primeira = context.getBean(PedidoService.class);
        PedidoService segunda = context.getBean(PedidoService.class);

        assertThat(primeira).isSameAs(segunda);
        assertThat(primeira).isSameAs(pedidoServiceInjetado);
        assertThat(context.getBeanFactory().getBeanDefinition("pedidoService").isSingleton()).isTrue();
    }

    @Test
    @DisplayName("Controllers e repositories também são singletons")
    void controllersERepositoriesSaoSingletons() {
        assertThat(context.getBean(PedidoController.class)).isSameAs(context.getBean(PedidoController.class));
        assertThat(context.getBean(PedidoRepository.class)).isSameAs(context.getBean(PedidoRepository.class));
    }

    @Test
    @DisplayName("Bean @Scope(\"singleton\") é o mesmo objeto que o Singleton clássico")
    void beanDoSpringEhOSingletonClassico() {
        assertThat(context.getBean(ConfiguracaoLoja.class)).isSameAs(ConfiguracaoLoja.INSTANCE);
        assertThat(context.getBean(GeradorNumeroPedido.class)).isSameAs(GeradorNumeroPedido.getInstance());
        assertThat(context.isSingleton("configuracaoLoja")).isTrue();
    }

    @Test
    @DisplayName("Contraste: escopo prototype cria uma nova instância a cada getBean")
    void prototypeCriaNovaInstancia() {
        CarrinhoTemporario primeiro = context.getBean(CarrinhoTemporario.class);
        CarrinhoTemporario segundo = context.getBean(CarrinhoTemporario.class);

        assertThat(primeiro).isNotSameAs(segundo);
        assertThat(context.isPrototype("carrinhoTemporario")).isTrue();
    }
}
