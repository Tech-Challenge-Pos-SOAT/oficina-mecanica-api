package com.postech.oficinamecanica.interfaces.rest.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthErrorResponse(
    @Schema(description = "Mensagem de erro genérica, sem indicar qual credencial falhou", example = "Credenciais inválidas")
    String message
) {}
