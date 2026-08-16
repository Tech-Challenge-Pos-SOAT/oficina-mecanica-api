package com.postech.oficinamecanica.interfaces.rest.vehicle;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateVehicleRequest(
    @Schema(description = "Identificador do cliente proprietário", example = "1")
    @NotNull Long customerId,

    @Schema(description = "Marca do veículo", example = "Toyota")
    @NotBlank String brand,

    @Schema(description = "Modelo do veículo", example = "Corolla")
    @NotBlank String model,

    @Schema(description = "Placa (formato antigo ou Mercosul, com ou sem traço)", example = "ABC-1234")
    @NotBlank String plate,

    @Schema(description = "Ano do veículo", example = "2021")
    @NotNull Integer year
) {}
