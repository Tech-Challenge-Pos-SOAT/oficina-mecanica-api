package com.postech.oficinamecanica.interfaces.rest.serviceorder;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record AddServiceRequest(
    @Schema(description = "Servico do catalogo", example = "1")
    @NotNull Long serviceId,

    @Schema(description = "Funcionario responsavel", example = "1")
    @NotNull Long employeeId
) {}
