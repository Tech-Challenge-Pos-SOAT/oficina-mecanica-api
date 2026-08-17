package com.postech.oficinamecanica.interfaces.rest.service;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ChangeServiceStatusRequest(
    @Schema(description = "Novo status do serviço", example = "INACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
    @NotBlank String status
) {}
