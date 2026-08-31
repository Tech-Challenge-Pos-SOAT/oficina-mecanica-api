package com.postech.oficinamecanica.interfaces.rest.serviceorder;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CancelServiceOrderRequest(
    @Schema(description = "Funcionario responsavel pelo cancelamento", example = "1")
    @NotNull Long employeeId,

    @Schema(description = "Motivo do cancelamento", example = "Cliente desistiu do reparo")
    @NotBlank @Size(max = 500) String reason
) {}
