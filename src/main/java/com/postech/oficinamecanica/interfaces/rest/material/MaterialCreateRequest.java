package com.postech.oficinamecanica.interfaces.rest.material;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record MaterialCreateRequest(
    @NotBlank(message = "Name is required")
    @Schema(description = "Nome do material", example = "Amortecedor Dianteiro")
    String name,

    @Schema(description = "Descrição opcional", example = "Peça original de fábrica")
    String description,

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price cannot be negative")
    @Schema(description = "Preço", example = "450.00")
    BigDecimal price,

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Schema(description = "Quantidade inicial em estoque", example = "10")
    Integer stockQuantity,

    @NotNull(message = "Stock minimum is required")
    @Min(value = 0, message = "Stock minimum cannot be negative")
    @Schema(description = "Estoque mínimo permitido", example = "2")
    Integer stockMinimum
) {}
