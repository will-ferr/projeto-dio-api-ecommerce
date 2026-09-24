package br.com.dio.ecommerce.singleton;

import java.math.BigDecimal;

/**
 * SINGLETON CLÁSSICO (Java puro) — implementação com <b>enum</b>.
 *
 * <p>Guarda as configurações globais da loja: nome, moeda, valor mínimo de pedido
 * e taxa de frete fixa. Só faz sentido existir UMA configuração da loja na aplicação,
 * então a própria classe garante isso.</p>
 *
 * <h2>Por que enum?</h2>
 * <ul>
 *   <li><b>Thread-safe de graça:</b> a JVM inicializa as constantes do enum uma única vez,
 *       durante a inicialização da classe, que é sincronizada pela própria JVM.</li>
 *   <li><b>Construtor implicitamente privado:</b> ninguém consegue fazer {@code new ConfiguracaoLoja()}.</li>
 *   <li><b>À prova de reflection:</b> {@code Constructor.newInstance} lança exceção para enums.</li>
 *   <li><b>À prova de serialização:</b> desserializar um enum devolve a mesma constante,
 *       sem criar uma segunda instância.</li>
 * </ul>
 * É a forma recomendada por Joshua Bloch (Effective Java, Item 3).
 *
 * <h2>Cuidado com estado global mutável</h2>
 * Todos os campos são {@code final}: a configuração é <b>imutável</b>. Um singleton com
 * estado mutável vira uma "variável global" — qualquer parte do código pode alterá-lo,
 * gerando bugs difíceis de rastrear e testes que interferem uns nos outros.
 *
 * <p>Obs.: em um projeto real esses valores viriam de {@code application.yml}
 * ({@code @ConfigurationProperties}); aqui ficam fixos para manter o exemplo didático.</p>
 */
public enum ConfiguracaoLoja {

    INSTANCE;

    private final String nomeLoja = "DIO Store";
    private final String moeda = "BRL";
    private final BigDecimal valorMinimoPedido = new BigDecimal("50.00");
    private final BigDecimal taxaFrete = new BigDecimal("15.90");

    /**
     * Ponto de acesso global. Com enum, {@code ConfiguracaoLoja.INSTANCE} já bastaria;
     * o método existe para deixar a API idêntica à do Singleton "tradicional".
     */
    public static ConfiguracaoLoja getInstance() {
        return INSTANCE;
    }

    public boolean atingeValorMinimo(BigDecimal subtotal) {
        return subtotal.compareTo(valorMinimoPedido) >= 0;
    }

    public BigDecimal calcularFrete(BigDecimal subtotal) {
        return taxaFrete;
    }

    public String getNomeLoja() {
        return nomeLoja;
    }

    public String getMoeda() {
        return moeda;
    }

    public BigDecimal getValorMinimoPedido() {
        return valorMinimoPedido;
    }

    public BigDecimal getTaxaFrete() {
        return taxaFrete;
    }
}
