package com.postech.oficinamecanica.interfaces.rest.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateCustomerRequest(
    @Schema(description = "Nome completo do cliente", example = "João Silva Santos")
    @NotBlank String name,

    @Schema(description = "Telefone com DDD", example = "(31) 99123-4567")
    @NotBlank String phone,

    @Schema(description = "Email do cliente (único)", example = "joao.silva@email.com")
    @NotBlank @Email String email
) {}
