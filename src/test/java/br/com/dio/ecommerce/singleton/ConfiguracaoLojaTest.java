package br.com.dio.ecommerce.singleton;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfiguracaoLojaTest {

    @Test
    @DisplayName("getInstance() retorna sempre a mesma instância")
    void getInstanceRetornaSempreAMesmaInstancia() {
        ConfiguracaoLoja primeira = ConfiguracaoLoja.getInstance();
        ConfiguracaoLoja segunda = ConfiguracaoLoja.getInstance();

        assertThat(primeira).isSameAs(segunda);
        assertThat(primeira).isSameAs(ConfiguracaoLoja.INSTANCE);
    }

    @Test
    @DisplayName("Várias threads obtêm a mesma instância")
    void mesmaInstanciaEntreThreads() throws InterruptedException {
        Set<Integer> identidades = ConcurrentHashMap.newKeySet();
        try (ExecutorService executor = Executors.newFixedThreadPool(8)) {
            for (int i = 0; i < 100; i++) {
                executor.submit(() -> identidades.add(System.identityHashCode(ConfiguracaoLoja.getInstance())));
            }
            executor.shutdown();
            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        }

        assertThat(identidades).hasSize(1);
    }

    @Test
    @DisplayName("Não é possível criar outra instância via reflection")
    void reflectionNaoCriaNovaInstancia() {
        Constructor<?> construtor = ConfiguracaoLoja.class.getDeclaredConstructors()[0];
        construtor.setAccessible(true);

        assertThatThrownBy(() -> construtor.newInstance("OUTRA", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("enum");
    }

    @Test
    @DisplayName("Regras de valor mínimo e frete")
    void regrasDeNegocio() {
        ConfiguracaoLoja config = ConfiguracaoLoja.getInstance();

        assertThat(config.atingeValorMinimo(new BigDecimal("49.99"))).isFalse();
        assertThat(config.atingeValorMinimo(new BigDecimal("50.00"))).isTrue();
        assertThat(config.calcularFrete(new BigDecimal("100.00"))).isEqualByComparingTo("15.90");
    }
}
