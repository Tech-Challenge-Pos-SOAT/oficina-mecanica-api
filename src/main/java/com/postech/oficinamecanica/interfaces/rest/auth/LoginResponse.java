package com.postech.oficinamecanica.interfaces.rest.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
    @Schema(description = "Token JWT", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String token,

    @Schema(description = "Tipo do token", example = "Bearer")
    String type,

    @Schema(description = "Identificador do funcionário autenticado", example = "1")
    Long employeeId,

    @Schema(description = "Nome do funcionário", example = "Carlos Souza")
    String name,

    @Schema(description = "Cargo do funcionário", example = "ATTENDANT")
    String role
) {}
