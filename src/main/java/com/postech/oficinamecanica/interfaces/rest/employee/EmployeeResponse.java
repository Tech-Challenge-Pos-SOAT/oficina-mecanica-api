package com.postech.oficinamecanica.interfaces.rest.employee;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

public record EmployeeResponse(
    @Schema(description = "Identificador único", example = "1")
    Long id,

    @Schema(description = "Nome completo do funcionário", example = "Carlos Souza")
    String name,

    @Schema(description = "Email único do funcionário", example = "carlos.souza@oficina.com")
    String email,

    @Schema(description = "Cargo do funcionário", example = "MECHANIC", allowableValues = {"MECHANIC", "ATTENDANT"})
    String role,

    @Schema(description = "Status do funcionário", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
    String status,

    @Schema(description = "Data e hora de criação (ISO-8601)", example = "2026-08-15T10:00:00Z")
    Instant createdAt,

    @Schema(description = "Data e hora da última atualização (ISO-8601)", example = "2026-08-15T10:30:00Z")
    Instant updatedAt
) {}
