package com.postech.oficinamecanica.interfaces.rest.serviceorder;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record EmployeeActionRequest(
    @Schema(description = "Funcionario responsavel pela acao", example = "1")
    @NotNull Long employeeId
) {}
