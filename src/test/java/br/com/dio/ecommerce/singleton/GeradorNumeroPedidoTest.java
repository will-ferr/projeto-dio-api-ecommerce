package br.com.dio.ecommerce.singleton;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeradorNumeroPedidoTest {

    @Test
    @DisplayName("getInstance() retorna sempre a mesma instância")
    void getInstanceRetornaSempreAMesmaInstancia() {
        assertThat(GeradorNumeroPedido.getInstance()).isSameAs(GeradorNumeroPedido.getInstance());
    }

    @Test
    @DisplayName("Número segue o formato PED-<ano>-000000")
    void formatoDoNumero() {
        String numero = GeradorNumeroPedido.getInstance().proximoNumero();

        assertThat(numero).matches("PED-" + Year.now().getValue() + "-\\d{6}");
    }

    @Test
    @DisplayName("Números gerados em várias threads são únicos (sem duplicados)")
    void numerosUnicosEmConcorrencia() throws Exception {
        int threads = 16;
        int numerosPorThread = 1_000;
        CountDownLatch largada = new CountDownLatch(1);

        List<Future<List<String>>> resultados = new ArrayList<>();
        try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
            Callable<List<String>> tarefa = () -> {
                largada.await(); // todas as threads começam juntas, maximizando a disputa
                GeradorNumeroPedido gerador = GeradorNumeroPedido.getInstance();
                List<String> gerados = new ArrayList<>(numerosPorThread);
                for (int i = 0; i < numerosPorThread; i++) {
                    gerados.add(gerador.proximoNumero());
                }
                return gerados;
            };
            for (int i = 0; i < threads; i++) {
                resultados.add(executor.submit(tarefa));
            }
            largada.countDown();

            List<String> todos = new ArrayList<>();
            for (Future<List<String>> resultado : resultados) {
                todos.addAll(resultado.get());
            }

            Set<String> unicos = new HashSet<>(todos);
            assertThat(todos).hasSize(threads * numerosPorThread);
            assertThat(unicos).hasSize(todos.size());
        }
    }

    @Test
    @DisplayName("Construtor privado bloqueia criação via reflection")
    void reflectionNaoCriaNovaInstancia() throws Exception {
        GeradorNumeroPedido.getInstance(); // garante que o Holder já foi inicializado
        Constructor<GeradorNumeroPedido> construtor = GeradorNumeroPedido.class.getDeclaredConstructor();
        construtor.setAccessible(true);

        assertThatThrownBy(construtor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(IllegalStateException.class);
    }
}
