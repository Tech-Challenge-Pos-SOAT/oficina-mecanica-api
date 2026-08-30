package com.postech.oficinamecanica.interfaces.rest.serviceorder;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddMaterialRequest(
    @Schema(description = "Material do catalogo", example = "1")
    @NotNull Long materialId,

    @Schema(description = "Quantidade necessaria", example = "2")
    @NotNull @Positive Integer quantity,

    @Schema(description = "Funcionario responsavel", example = "1")
    @NotNull Long employeeId
) {}
