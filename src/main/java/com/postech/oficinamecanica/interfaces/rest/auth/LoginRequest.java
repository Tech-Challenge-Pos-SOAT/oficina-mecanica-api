package com.postech.oficinamecanica.interfaces.rest.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @Schema(description = "Email do funcionário", example = "carlos.souza@oficina.com")
    @NotBlank @Email String email,

    @Schema(description = "Senha em texto puro", example = "senha123")
    @NotBlank String password
) {}
