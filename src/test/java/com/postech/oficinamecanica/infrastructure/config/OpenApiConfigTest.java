package com.postech.oficinamecanica.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    @Test
    void shouldBuildOpenApiWithTitleAndVersion() {
        OpenAPI openAPI = new OpenApiConfig().customOpenAPI();

        assertThat(openAPI.getInfo().getTitle()).isEqualTo("Oficina Mecanica API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.0.0");
    }
}
