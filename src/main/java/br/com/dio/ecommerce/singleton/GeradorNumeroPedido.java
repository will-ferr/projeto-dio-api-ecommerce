package br.com.dio.ecommerce.singleton;

import java.time.Year;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SINGLETON CLÁSSICO (Java puro) — implementação com o idiom <b>Holder (Bill Pugh)</b>.
 *
 * <p>Gera números de pedido sequenciais e únicos no formato {@code PED-2026-000001}.</p>
 *
 * <h2>Por que precisa ser uma instância única?</h2>
 * O contador ({@link AtomicLong}) é o estado do gerador. Se existissem duas instâncias,
 * cada uma teria o SEU contador começando do zero, e ambas gerariam {@code PED-2026-000001}
 * — números duplicados. Ter uma única instância garante uma única fonte da verdade
 * para a sequência.
 *
 * <h2>Como o Holder garante thread-safety sem {@code synchronized}?</h2>
 * A classe interna {@code Holder} só é carregada pela JVM na primeira chamada de
 * {@link #getInstance()} (<i>lazy loading</i>). A inicialização de classes é feita pela
 * JVM de forma sincronizada (JLS §12.4.2), então a instância é criada exatamente uma vez,
 * mesmo com várias threads chamando {@code getInstance()} ao mesmo tempo — sem o custo de
 * locks a cada acesso e sem as armadilhas do <i>double-checked locking</i>.
 *
 * <h2>Por que AtomicLong?</h2>
 * Ser instância única não basta: várias threads (requisições HTTP) usam o MESMO objeto
 * ao mesmo tempo. {@code contador++} em um {@code long} comum não é atômico (ler, somar,
 * gravar) e perderia incrementos. {@link AtomicLong#incrementAndGet()} usa CAS
 * (compare-and-swap) e é atômico.
 *
 * <h2>Limitação</h2>
 * O contador vive na memória de UMA JVM. Com várias réplicas da aplicação (cluster),
 * cada uma teria seu singleton e os números colidiriam. Nesse cenário o correto é usar
 * uma sequence do banco de dados ou um serviço de IDs distribuído. "Único" em um Singleton
 * significa único por ClassLoader/JVM, não único no sistema todo.
 */
public final class GeradorNumeroPedido {

    private static final String FORMATO = "PED-%d-%06d";

    private final AtomicLong sequencia = new AtomicLong(0);

    /** Construtor privado: ninguém de fora cria instâncias. */
    private GeradorNumeroPedido() {
        // Proteção extra contra criação via reflection (setAccessible(true)).
        // Durante a criação legítima, Holder.INSTANCE ainda é null.
        if (Holder.INSTANCE != null) {
            throw new IllegalStateException("GeradorNumeroPedido é um Singleton: use getInstance()");
        }
    }

    /** Só é carregada (e a instância criada) no primeiro acesso a getInstance(). */
    private static final class Holder {
        private static final GeradorNumeroPedido INSTANCE = new GeradorNumeroPedido();
    }

    public static GeradorNumeroPedido getInstance() {
        return Holder.INSTANCE;
    }

    public String proximoNumero() {
        return FORMATO.formatted(Year.now().getValue(), sequencia.incrementAndGet());
    }
}
