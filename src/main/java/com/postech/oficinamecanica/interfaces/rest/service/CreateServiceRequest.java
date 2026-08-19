package com.postech.oficinamecanica.interfaces.rest.service;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateServiceRequest(
    @Schema(description = "Nome do serviço, único no sistema", example = "Troca de óleo")
    @NotBlank String name,

    @Schema(description = "Descrição do serviço", example = "Troca de óleo do motor com filtro incluso")
    String description,

    @Schema(description = "Preço do serviço, maior que zero", example = "120.00")
    @NotNull @Positive BigDecimal price
) {}
