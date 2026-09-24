# E-commerce API — Padrão de Projeto Singleton

API REST de um e-commerce construída com **Spring Boot 3** para demonstrar o padrão de projeto **Singleton** de duas formas:

1. **Singleton clássico (Java puro)**: a própria classe garante que só existe uma instância.
2. **Singleton gerenciado pelo Spring**: o container (ApplicationContext) garante isso.

O domínio (categorias, produtos, clientes e pedidos) serve de cenário real para mostrar **onde** e **por que** cada tipo de Singleton é usado.

## Stack

| Tecnologia | Uso |
|---|---|
| Java 21 | Linguagem (records, switch expressions, text blocks) |
| Spring Boot 3.5 | Spring Web, Spring Data JPA, Bean Validation |
| H2 | Banco em memória, com console web |
| Springdoc OpenAPI | Documentação e Swagger UI |
| JUnit 5, AssertJ, Mockito, MockMvc | Testes |
| Maven | Build |

## Estrutura de pacotes

```
src/main/java/br/com/dio/ecommerce
├── EcommerceApplication.java
├── config/        SingletonConfig (beans @Scope("singleton")), OpenApiConfig, DataLoader
├── controller/    Categoria, Produto, Cliente, Pedido, Loja
├── dto/           Records de entrada (Request) e saída (Response)
├── exception/     Exceções de negócio + GlobalExceptionHandler (@RestControllerAdvice)
├── model/         Entidades JPA + enum StatusPedido
├── repository/    Interfaces Spring Data JPA
├── service/       Regras de negócio
└── singleton/     ConfiguracaoLoja (enum) e GeradorNumeroPedido (Holder)
```

---

## O padrão Singleton

### Que problema ele resolve?

Alguns objetos **precisam** existir uma única vez na aplicação, porque várias cópias causariam inconsistência ou desperdício:

- **Um gerador de números sequenciais**: duas instâncias teriam dois contadores e gerariam números repetidos.
- **Configurações globais**: todos os pontos do sistema precisam ver os mesmos valores.
- **Recursos caros de criar**, como pools de conexão, caches e clientes HTTP.

O Singleton garante **uma única instância** e oferece **um ponto de acesso global** a ela.

### Quando usar

- Existe, de fato, uma única instância lógica do conceito (a configuração da loja, a sequência de pedidos).
- O objeto é caro de criar e pode ser compartilhado com segurança.
- O objeto é **imutável** ou **thread-safe**.

### Quando evitar

- **Estado global mutável.** Qualquer parte do código pode alterar o objeto. Os bugs ficam difíceis de rastrear e o comportamento passa a depender da ordem de execução.
- **Dificuldade de testar.** Um `Classe.getInstance()` chamado no meio da lógica fica acoplado à implementação concreta: não dá para trocá-lo por um mock ou fake. Além disso, o estado persiste entre testes (o contador do gerador não "zera").
- **Dependências ocultas.** Quem lê a assinatura de um método não percebe que ele depende de um singleton.
- **Ambientes distribuídos.** "Único" significa único **por JVM/ClassLoader**. Com várias réplicas da aplicação, cada uma tem o seu singleton. Um contador em memória não garante unicidade em um cluster: nesse caso use uma sequence do banco ou um gerador de IDs distribuído.

> Neste projeto, os problemas acima são mitigados: a `ConfiguracaoLoja` é **imutável**, o `GeradorNumeroPedido` é **thread-safe** e os dois são **injetados** nos services, que não chamam `getInstance()`.

---

## Onde o padrão aparece no código

### 1. Singleton clássico (Java puro)

| Classe | Implementação | Por quê |
|---|---|---|
| [`singleton/ConfiguracaoLoja.java`](src/main/java/br/com/dio/ecommerce/singleton/ConfiguracaoLoja.java) | **enum Singleton** | Nome da loja, moeda, valor mínimo do pedido (R$ 50,00) e frete fixo (R$ 15,90). É imutável. O enum é thread-safe, tem construtor implicitamente privado e resiste a reflection e serialização (Effective Java, Item 3). |
| [`singleton/GeradorNumeroPedido.java`](src/main/java/br/com/dio/ecommerce/singleton/GeradorNumeroPedido.java) | **Holder idiom (Bill Pugh)** + `AtomicLong` | Gera `PED-2026-000001`, `PED-2026-000002`… O Holder dá *lazy loading* e thread-safety sem `synchronized`, porque a JVM inicializa a classe interna uma única vez. O `AtomicLong` garante incrementos atômicos entre threads. |

**Por que o `GeradorNumeroPedido` precisa ser único?** O contador **é** o estado do gerador. Se houvesse duas instâncias, cada uma começaria em zero e as duas emitiriam `PED-2026-000001`. Com uma instância só, existe uma única fonte da verdade para a sequência.

**Por que só a instância única não basta?** Várias requisições HTTP usam o **mesmo** objeto ao mesmo tempo. `contador++` não é atômico (ler → somar → gravar) e perderia incrementos, por isso usamos `AtomicLong.incrementAndGet()`.

### 2. Singleton gerenciado pelo Spring

| Onde | O que demonstra |
|---|---|
| [`config/SingletonConfig.java`](src/main/java/br/com/dio/ecommerce/config/SingletonConfig.java) | Declara `ConfiguracaoLoja` com `@Bean @Scope("singleton")` explícito (didático, pois é o padrão) e `GeradorNumeroPedido` como `@Bean`. Faz a **ponte** entre os singletons clássicos e o container. |
| `service/*Service.java` (`@Service`) | Singletons por padrão: não guardam estado de requisição em campos. |
| `repository/*Repository.java` | O Spring cria **um** proxy por interface. |
| `controller/*Controller.java` (`@RestController`) | Uma instância atende todas as requisições. |
| [`exception/GlobalExceptionHandler.java`](src/main/java/br/com/dio/ecommerce/exception/GlobalExceptionHandler.java) | Também é um bean singleton. |
| [`service/PedidoService.java`](src/main/java/br/com/dio/ecommerce/service/PedidoService.java) | É onde os dois tipos se encontram: um singleton do Spring que **recebe por injeção** os dois singletons clássicos. Usa a `ConfiguracaoLoja` para validar o valor mínimo e calcular o frete, e o `GeradorNumeroPedido` para numerar o pedido. |

### Testes que provam o comportamento

| Teste | O que prova |
|---|---|
| [`singleton/ConfiguracaoLojaTest`](src/test/java/br/com/dio/ecommerce/singleton/ConfiguracaoLojaTest.java) | `getInstance()` sempre retorna a mesma instância, inclusive entre threads. Reflection não cria outra instância. |
| [`singleton/GeradorNumeroPedidoTest`](src/test/java/br/com/dio/ecommerce/singleton/GeradorNumeroPedidoTest.java) | 16 threads geram 16.000 números, todos únicos. O formato está correto e reflection é bloqueada. |
| [`SpringSingletonScopeTest`](src/test/java/br/com/dio/ecommerce/SpringSingletonScopeTest.java) | `getBean(PedidoService.class)` chamado duas vezes retorna a mesma instância. O bean `configuracaoLoja` é **o mesmo objeto** que `ConfiguracaoLoja.INSTANCE`. Para contraste, um bean `prototype` gera instâncias diferentes. |
| [`service/PedidoServiceTest`](src/test/java/br/com/dio/ecommerce/service/PedidoServiceTest.java) | Fluxo do pedido com Mockito: cálculo do total, baixa de estoque, estoque insuficiente, valor mínimo, cliente inexistente, cancelamento e transição inválida. |
| [`controller/PedidoControllerIntegrationTest`](src/test/java/br/com/dio/ecommerce/controller/PedidoControllerIntegrationTest.java) | Fluxo HTTP completo com MockMvc e H2, incluindo as respostas 201, 400, 404 e 422. |

---

## Singleton clássico × singleton do Spring

| | Singleton clássico (GoF) | Singleton do Spring |
|---|---|---|
| **Quem garante a unicidade** | A própria classe (construtor privado + `getInstance()`) | O container (`ApplicationContext`) |
| **Escopo da unicidade** | Uma instância por **ClassLoader/JVM** | Uma instância por **ApplicationContext** (dois contexts criam duas instâncias) |
| **Construtor** | Privado | Normalmente público: nada impede um `new`, mas o container reutiliza a instância dele |
| **Como obter** | `Classe.getInstance()`, com acoplamento direto | Injeção de dependência pelo construtor |
| **Testabilidade** | Difícil de substituir, e o estado vaza entre testes | Fácil: basta passar outra implementação no construtor |
| **Ciclo de vida** | Vive até o ClassLoader ser descarregado | Gerenciado pelo container, com callbacks como `@PostConstruct` e `@PreDestroy` |

**Na prática:** em aplicações Spring, prefira beans (singleton por padrão) a singletons clássicos. Use o singleton clássico para código fora do container (bibliotecas, utilitários) ou quando a unicidade precisa valer para a JVM inteira. Neste projeto os dois são combinados: os singletons clássicos são registrados como beans em `SingletonConfig`, e os services recebem essas dependências **por injeção**.

> Beans singleton são compartilhados entre threads. Por isso **nunca guarde estado de requisição em campos** de `@Service` ou `@Controller`.

---

## Como rodar

**Pré-requisitos:** JDK 21 e Maven 3.9 ou superior.

```bash
# rodar os testes
mvn test

# subir a aplicação (porta 8080)
mvn spring-boot:run

# ou gerar o jar e executar
mvn package
java -jar target/ecommerce-singleton-1.0.0.jar
```

Na inicialização, o [`DataLoader`](src/main/java/br/com/dio/ecommerce/config/DataLoader.java) popula o banco com 3 categorias, 7 produtos e 3 clientes.

### Swagger UI

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI (JSON): http://localhost:8080/v3/api-docs

### Console do H2

- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:ecommerce`
- Usuário: `sa`
- Senha: *(vazia)*

---

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| GET / POST | `/categorias` | Lista ou cria categorias |
| GET / PUT / DELETE | `/categorias/{id}` | Busca, atualiza ou exclui uma categoria |
| GET / POST | `/produtos` | Lista ou cria produtos |
| GET / PUT / DELETE | `/produtos/{id}` | Busca, atualiza ou exclui um produto |
| GET / POST | `/clientes` | Lista ou cria clientes |
| GET / PUT / DELETE | `/clientes/{id}` | Busca, atualiza ou exclui um cliente |
| POST | `/pedidos` | Cria um pedido (valida o estoque e calcula o total com frete) |
| GET | `/pedidos`, `/pedidos/{id}` | Lista pedidos ou busca um pedido |
| PATCH | `/pedidos/{id}/status` | Atualiza o status |
| GET | `/loja/configuracoes` | Configurações da loja (Singleton) |

**Transições de status:** `CRIADO → PAGO | CANCELADO`, `PAGO → ENVIADO | CANCELADO`, `ENVIADO → ENTREGUE`. Cancelar um pedido devolve os itens ao estoque.

### Respostas de erro padronizadas

| Status | Quando |
|---|---|
| 400 | Falha de Bean Validation, JSON mal formado ou tipo de parâmetro inválido |
| 404 | Recurso não encontrado |
| 422 | Regra de negócio violada: estoque insuficiente, valor mínimo, transição de status inválida, e-mail ou CPF duplicado |
| 409 | Conflito de concorrência (lock otimista no estoque do produto) |

```json
{
  "timestamp": "2026-09-23T23:13:09.27-03:00",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Dados de entrada inválidos",
  "path": "/clientes",
  "campos": [
    { "campo": "email", "mensagem": "deve ser um endereço de e-mail bem formado" }
  ]
}
```

---

## Exemplos de requisições

**Configurações da loja**

```bash
curl http://localhost:8080/loja/configuracoes
```
```json
{ "nomeLoja": "DIO Store", "moeda": "BRL", "valorMinimoPedido": 50.00, "taxaFrete": 15.90 }
```

**Criar um pedido**

```bash
curl -X POST http://localhost:8080/pedidos \
  -H "Content-Type: application/json" \
  -d '{"clienteId": 1, "itens": [{"produtoId": 4, "quantidade": 2}, {"produtoId": 7, "quantidade": 1}]}'
```
```json
{
  "id": 1,
  "numero": "PED-2026-000001",
  "cliente": { "id": 1, "nome": "Ana Souza", "email": "ana.souza@email.com", "cpf": "52998224725" },
  "itens": [
    { "id": 1, "produtoId": 4, "produtoNome": "Clean Code", "quantidade": 2, "precoUnitario": 119.90, "subtotal": 239.80 },
    { "id": 2, "produtoId": 7, "produtoNome": "Caneca Java", "quantidade": 1, "precoUnitario": 39.90, "subtotal": 39.90 }
  ],
  "subtotal": 279.70,
  "valorFrete": 15.90,
  "valorTotal": 295.60,
  "status": "CRIADO",
  "dataCriacao": "2026-09-23T23:13:08.69"
}
```

**Pedido abaixo do valor mínimo (422)**

```bash
curl -X POST http://localhost:8080/pedidos \
  -H "Content-Type: application/json" \
  -d '{"clienteId": 1, "itens": [{"produtoId": 7, "quantidade": 1}]}'
```
```json
{ "status": 422, "erro": "Unprocessable Entity", "mensagem": "Valor mínimo do pedido é BRL 50.00; subtotal atual: BRL 39.90", "path": "/pedidos" }
```

**Atualizar o status**

```bash
curl -X PATCH http://localhost:8080/pedidos/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "PAGO"}'
```

**Criar um produto**

```bash
curl -X POST http://localhost:8080/produtos \
  -H "Content-Type: application/json" \
  -d '{"nome": "Teclado Mecânico", "descricao": "Switch brown", "preco": 399.90, "estoque": 12, "categoriaId": 1}'
```

**Criar um cliente** (o CPF precisa ser válido e ter só os 11 dígitos)

```bash
curl -X POST http://localhost:8080/clientes \
  -H "Content-Type: application/json" \
  -d '{"nome": "Diego Alves", "email": "diego@email.com", "cpf": "98765432100"}'
```

---

## Decisões de design

- **Enum para a `ConfiguracaoLoja`, Holder para o `GeradorNumeroPedido`.** São as duas implementações thread-safe mais usadas, e cada uma aparece aqui. O enum é a forma mais robusta. O Holder mostra a mecânica clássica: construtor privado, `getInstance()` e *lazy loading*.
- **Injeção em vez de `getInstance()` nos services.** A lógica de negócio fica testável e desacoplada. O teste do `PedidoService` não precisa subir o Spring.
- **Configuração imutável.** Evita o principal problema do Singleton, que é o estado global mutável.
- **DTOs como records.** A API não expõe as entidades JPA, o que evita problemas com lazy loading e serialização circular. `open-in-view` está desativado e o mapeamento acontece dentro da transação.
- **Preço congelado no `ItemPedido`.** Se o preço do produto mudar depois, o pedido não é afetado.
- **`@Version` no `Produto`.** O lock otimista evita que duas compras simultâneas deixem o estoque negativo.
- **`CommandLineRunner` em vez de `data.sql`.** Os IDs são gerados pelo banco e não entram em conflito com os registros criados pela API.
