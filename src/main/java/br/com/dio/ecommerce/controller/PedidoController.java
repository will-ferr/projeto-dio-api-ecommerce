package br.com.dio.ecommerce.controller;

import br.com.dio.ecommerce.dto.AtualizarStatusRequest;
import br.com.dio.ecommerce.dto.PedidoRequest;
import br.com.dio.ecommerce.dto.PedidoResponse;
import br.com.dio.ecommerce.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Tag(name = "Pedidos")
@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @Operation(summary = "Cria um pedido",
            description = "Valida estoque, aplica valor mínimo e frete da ConfiguracaoLoja (Singleton) "
                    + "e gera o número com o GeradorNumeroPedido (Singleton).")
    @PostMapping
    public ResponseEntity<PedidoResponse> criar(@RequestBody @Valid PedidoRequest request) {
        PedidoResponse criado = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @GetMapping
    public List<PedidoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public PedidoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @Operation(summary = "Atualiza o status do pedido",
            description = "Transições: CRIADO→PAGO|CANCELADO, PAGO→ENVIADO|CANCELADO, ENVIADO→ENTREGUE. "
                    + "Cancelar devolve os itens ao estoque.")
    @PatchMapping("/{id}/status")
    public PedidoResponse atualizarStatus(@PathVariable Long id, @RequestBody @Valid AtualizarStatusRequest request) {
        return service.atualizarStatus(id, request.status());
    }
}
