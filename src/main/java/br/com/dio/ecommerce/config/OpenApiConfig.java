package br.com.dio.ecommerce.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("E-commerce API — Padrão Singleton")
                .version("1.0.0")
                .description("API REST de e-commerce que demonstra o Singleton clássico (Java puro) "
                        + "e o singleton gerenciado pelo container do Spring."));
    }
}
