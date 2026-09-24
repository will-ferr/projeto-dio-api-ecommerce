package br.com.dio.ecommerce.config;

import br.com.dio.ecommerce.singleton.ConfiguracaoLoja;
import br.com.dio.ecommerce.singleton.GeradorNumeroPedido;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * SINGLETON GERENCIADO PELO SPRING.
 *
 * <h2>Singleton clássico x singleton do Spring</h2>
 * <table>
 *   <tr><th></th><th>Clássico (GoF)</th><th>Spring (escopo singleton)</th></tr>
 *   <tr><td>Quem garante a unicidade?</td>
 *       <td>A própria classe (construtor privado + getInstance())</td>
 *       <td>O container (ApplicationContext)</td></tr>
 *   <tr><td>Unicidade por...</td>
 *       <td>ClassLoader / JVM</td>
 *       <td>ApplicationContext (dois contexts = duas instâncias)</td></tr>
 *   <tr><td>Como obter?</td>
 *       <td>{@code Classe.getInstance()} — acoplamento direto</td>
 *       <td>Injeção de dependência pelo construtor</td></tr>
 *   <tr><td>Testabilidade</td>
 *       <td>Difícil trocar por um mock/fake</td>
 *       <td>Fácil: basta passar outra implementação no construtor</td></tr>
 * </table>
 *
 * <p>No Spring, TODO bean é singleton por padrão: {@code @Service}, {@code @Repository},
 * {@code @RestController}, {@code @Component} e {@code @Bean}. A classe em si continua
 * tendo construtor público — nada impede um {@code new PedidoService(...)} — mas o
 * container cria uma única instância e a reaproveita em todos os pontos de injeção.</p>
 *
 * <p>Aqui fazemos a ponte entre os dois mundos: os singletons clássicos são registrados
 * como beans. Assim os services recebem essas dependências por injeção (desacoplados de
 * {@code getInstance()}) e, nos testes, podemos passar qualquer instância no construtor.</p>
 */
@Configuration
public class SingletonConfig {

    /**
     * {@code @Scope("singleton")} é o padrão e poderia ser omitido — está explícito apenas
     * para fins didáticos. Equivale a {@code ConfigurableBeanFactory.SCOPE_SINGLETON}.
     * Outros escopos: "prototype" (nova instância a cada injeção/getBean),
     * "request" e "session" (em aplicações web).
     *
     * <p>Como o método devolve {@code ConfiguracaoLoja.INSTANCE}, o bean do Spring e o
     * singleton clássico são literalmente o mesmo objeto.</p>
     */
    @Bean
    @Scope("singleton")
    public ConfiguracaoLoja configuracaoLoja() {
        return ConfiguracaoLoja.getInstance();
    }

    /**
     * Sem {@code @Scope}: por padrão o bean já é singleton. Mesmo que este método fosse
     * chamado várias vezes dentro desta classe, o Spring (via proxy CGLIB de
     * {@code @Configuration}) devolveria sempre a instância já criada no container.
     */
    @Bean
    public GeradorNumeroPedido geradorNumeroPedido() {
        return GeradorNumeroPedido.getInstance();
    }
}
