package com.postech.oficinamecanica.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  private static final String BEARER_SCHEME = "bearerAuth";

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
      .info(new Info()
        .title("Oficina Mecanica API")
        .version("1.0.0")
        .description("API REST para gerenciamento de ordens de servico, clientes, veiculos, servicos e materiais"))
      // Sem isso o Swagger UI nao tem botao "Authorize" e toda chamada em
      // /api/* volta 401 - o token vem de POST /auth/login.
      .components(new Components().addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
        .type(SecurityScheme.Type.HTTP)
        .scheme("bearer")
        .bearerFormat("JWT")))
      .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
  }
}
