package com.postech.oficinamecanica.interfaces.rest.service;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;

public record ServiceResponse(
    @Schema(description = "Identificador único", example = "1")
    Long id,

    @Schema(description = "Nome do serviço", example = "Troca de óleo")
    String name,

    @Schema(description = "Descrição do serviço", example = "Troca de óleo do motor com filtro incluso")
    String description,

    @Schema(description = "Preço do serviço", example = "120.00")
    BigDecimal price,

    @Schema(description = "Status do serviço", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
    String status,

    @Schema(description = "Data e hora de criação (ISO-8601)", example = "2026-08-15T10:00:00Z")
    Instant createdAt,

    @Schema(description = "Data e hora da última atualização (ISO-8601)", example = "2026-08-15T10:30:00Z")
    Instant updatedAt
) {}
