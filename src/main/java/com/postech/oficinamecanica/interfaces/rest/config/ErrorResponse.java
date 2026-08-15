package com.postech.oficinamecanica.interfaces.rest.config;

import io.swagger.v3.oas.annotations.media.Schema;

public record ErrorResponse(
    @Schema(description = "Código de erro", example = "INVALID_STATUS")
    String code,

    @Schema(description = "Mensagem de erro em português", example = "Status deve ser ACTIVE ou INACTIVE")
    String message,

    @Schema(description = "Status HTTP", example = "400")
    int status
) {}
