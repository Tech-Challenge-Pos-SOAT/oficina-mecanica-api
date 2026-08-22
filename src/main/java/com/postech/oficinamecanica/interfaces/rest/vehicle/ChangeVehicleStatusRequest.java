package com.postech.oficinamecanica.interfaces.rest.vehicle;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ChangeVehicleStatusRequest(
    @Schema(description = "Novo status do veículo", example = "INACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
    @NotBlank String status
) {}
