package com.postech.oficinamecanica.interfaces.rest.employee;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UpdateEmployeeStatusRequest(
    @Schema(description = "Novo status do funcionário", example = "INACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
    @NotBlank String status
) {}
