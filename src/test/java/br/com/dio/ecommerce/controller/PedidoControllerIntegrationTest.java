package br.com.dio.ecommerce.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Teste ponta a ponta (HTTP → controller → service → H2) usando os dados do DataLoader. */
@SpringBootTest
@AutoConfigureTestDatabase // banco H2 com nome único por contexto de teste
@AutoConfigureMockMvc
@Transactional
class PedidoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST /pedidos cria pedido e retorna 201")
    void criaPedido() throws Exception {
        mockMvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON).content("""
                        {"clienteId": 1, "itens": [{"produtoId": 2, "quantidade": 1}]}
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.numero").value(org.hamcrest.Matchers.startsWith("PED-")))
                .andExpect(jsonPath("$.status").value("CRIADO"))
                .andExpect(jsonPath("$.valorTotal").value(105.80));
    }

    @Test
    @DisplayName("POST /pedidos com corpo inválido retorna 400 com campos")
    void validacaoRetorna400() throws Exception {
        mockMvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON).content("""
                        {"itens": []}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.campos").isArray());
    }

    @Test
    @DisplayName("POST /pedidos com cliente inexistente retorna 404")
    void clienteInexistenteRetorna404() throws Exception {
        mockMvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON).content("""
                        {"clienteId": 999, "itens": [{"produtoId": 1, "quantidade": 1}]}
                        """))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /pedidos sem estoque retorna 422")
    void semEstoqueRetorna422() throws Exception {
        mockMvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON).content("""
                        {"clienteId": 1, "itens": [{"produtoId": 1, "quantidade": 1000}]}
                        """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("Estoque insuficiente")));
    }

    @Test
    @DisplayName("GET /loja/configuracoes retorna os dados do Singleton")
    void configuracoesDaLoja() throws Exception {
        mockMvc.perform(get("/loja/configuracoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeLoja").value("DIO Store"))
                .andExpect(jsonPath("$.moeda").value("BRL"))
                .andExpect(jsonPath("$.valorMinimoPedido").value(50.00))
                .andExpect(jsonPath("$.taxaFrete").value(15.90));
    }
}
