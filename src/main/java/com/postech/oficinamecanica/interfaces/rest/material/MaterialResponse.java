package com.postech.oficinamecanica.interfaces.rest.material;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;

public record MaterialResponse(
    @Schema(description = "Identificador único", example = "1")
    Long id,

    @Schema(description = "Nome do material", example = "Filtro de Óleo")
    String name,

    @Schema(description = "Descrição do material (opcional)", example = "Compatível com linha leve Fiat/VW")
    String description,

    @Schema(description = "Preço de catálogo vigente", example = "32.50")
    BigDecimal price,

    @Schema(description = "Saldo atual em estoque", example = "25")
    Integer stockQuantity,

    @Schema(description = "Estoque mínimo; abaixo disso o material precisa de reposição", example = "5")
    Integer stockMinimum,

    @Schema(description = "Status do material", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
    String status,

    @Schema(description = "Data e hora de criação (ISO-8601)", example = "2026-08-16T10:30:00Z")
    Instant createdAt,

    @Schema(description = "Data e hora da última atualização (ISO-8601)", example = "2026-08-16T10:30:00Z")
    Instant updatedAt
) {}
