package br.com.dio.ecommerce.controller;

import br.com.dio.ecommerce.dto.ConfiguracaoLojaResponse;
import br.com.dio.ecommerce.singleton.ConfiguracaoLoja;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Loja")
@RestController
@RequestMapping("/loja")
public class LojaController {

    /** Injetado pelo Spring: é o mesmo objeto que ConfiguracaoLoja.INSTANCE (ver SingletonConfig). */
    private final ConfiguracaoLoja configuracaoLoja;

    public LojaController(ConfiguracaoLoja configuracaoLoja) {
        this.configuracaoLoja = configuracaoLoja;
    }

    @Operation(summary = "Retorna as configurações da loja (Singleton ConfiguracaoLoja)")
    @GetMapping("/configuracoes")
    public ConfiguracaoLojaResponse configuracoes() {
        return ConfiguracaoLojaResponse.from(configuracaoLoja);
    }
}
