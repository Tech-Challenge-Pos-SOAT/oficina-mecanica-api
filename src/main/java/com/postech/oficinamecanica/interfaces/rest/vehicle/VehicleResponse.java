package com.postech.oficinamecanica.interfaces.rest.vehicle;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

public record VehicleResponse(
    @Schema(description = "Identificador único", example = "1")
    Long id,

    @Schema(description = "Identificador do cliente proprietário", example = "1")
    Long customerId,

    @Schema(description = "Marca do veículo", example = "Toyota")
    String brand,

    @Schema(description = "Modelo do veículo", example = "Corolla")
    String model,

    @Schema(description = "Placa formatada", example = "ABC-1234")
    String plate,

    @Schema(description = "Ano do veículo", example = "2021")
    int year,

    @Schema(description = "Status do veículo", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
    String status,

    @Schema(description = "Data e hora de criação (ISO-8601)", example = "2026-08-15T10:00:00Z")
    Instant createdAt,

    @Schema(description = "Data e hora da última atualização (ISO-8601)", example = "2026-08-15T10:00:00Z")
    Instant updatedAt
) {}
