package com.postech.oficinamecanica.interfaces.rest.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ChangeCustomerStatusRequest(
    @Schema(description = "Novo status do cliente", example = "INACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
    @NotBlank String status
) {}
