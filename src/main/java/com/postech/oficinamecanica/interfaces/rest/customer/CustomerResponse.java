package com.postech.oficinamecanica.interfaces.rest.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

public record CustomerResponse(
    @Schema(description = "Identificador único", example = "1")
    Long id,

    @Schema(description = "Nome completo do cliente", example = "João Silva Santos")
    String name,

    @Schema(description = "CPF ou CNPJ formatado", example = "123.456.789-01")
    String document,

    @Schema(description = "Telefone com DDD", example = "(31) 99123-4567")
    String phone,

    @Schema(description = "Email do cliente", example = "joao.silva@email.com")
    String email,

    @Schema(description = "Status do cliente", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
    String status,

    @Schema(description = "Data e hora de criação (ISO-8601)", example = "2026-08-15T10:30:00Z")
    Instant createdAt,

    @Schema(description = "Data e hora da última atualização (ISO-8601)", example = "2026-08-15T10:30:00Z")
    Instant updatedAt
) {}
